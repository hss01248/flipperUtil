/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.flipper.plugins.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import com.facebook.flipper.plugins.network.NetworkReporter.Header;
import com.facebook.flipper.plugins.network.NetworkReporter.ResponseInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * 图片响应: 以流拷贝方式完整写入临时文件,不阻塞下游读取;接收完毕后在后台读取 EXIF 并上报 Flipper。
 * 仅当缓存文件大于 {@link #THUMBNAIL_IF_FILE_LARGER_THAN_BYTES} 时才生成缩略图 JPEG,否则上报原始字节。
 */
public final class FlipperImageResponseUtil {

  /** 超过该大小时对 Flipper 使用缩略图,否则直接使用原图字节(含 ≤ 此大小) */
  public static final long THUMBNAIL_IF_FILE_LARGER_THAN_BYTES = 500L * 1024L;

  /** 缩略图 JPEG 短边像素 */
  public static final int THUMBNAIL_SHORT_SIDE = 256;
  /** 缩略图 JPEG 质量 0–100 */
  public static final int THUMBNAIL_JPEG_QUALITY = 75;

  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

  private FlipperImageResponseUtil() {}

  public interface ResponseInfoFactory {
    ResponseInfo create(
        Response response,
        @Nullable byte[] body,
        String requestId,
        boolean isMock,
        long requestStartMs,
        long responseReceivedMs);
  }

  public static boolean shouldHandleAsImageResponse(@Nullable Response response) {
    if (response == null || response.body() == null) {
      return false;
    }
    MediaType ct = response.body().contentType();
    if (ct == null) {
      return false;
    }
    return "image".equalsIgnoreCase(ct.type());
  }

  public static Response wrapForFlipper(
      final Response response,
      final NetworkFlipperPlugin plugin,
      final String requestId,
      final boolean isMock,
      final long requestStartMs,
      final long responseReceivedMs,
      final ResponseInfoFactory factory) {
    final ResponseBody originalBody = response.body();
    if (originalBody == null) {
      return response;
    }

    final File cacheFile;
    try {
      cacheFile =
          File.createTempFile(
              "flipper-img-", ".bin", new File(System.getProperty("java.io.tmpdir")));
    } catch (IOException e) {
      return response;
    }

    final BufferedSink fileSink;
    try {
      fileSink = Okio.buffer(Okio.sink(cacheFile));
    } catch (Exception e) {
      //noinspection ResultOfMethodCallIgnored
      cacheFile.delete();
      return response;
    }

    final MediaType contentType = originalBody.contentType();
    final long contentLength = originalBody.contentLength();

    ResponseBody wrappedBody =
        new ResponseBody() {
          @Override
          public MediaType contentType() {
            return contentType;
          }

          @Override
          public long contentLength() {
            return contentLength;
          }

          @Override
          public BufferedSource source() {
            return Okio.buffer(
                new ForwardingSource(originalBody.source()) {
                  @Override
                  public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    if (bytesRead > 0) {
                      Buffer tempBuffer = sink.clone();
                      long skipBytes = sink.size() - bytesRead;
                      if (skipBytes > 0) {
                        tempBuffer.skip(skipBytes);
                      }
                      fileSink.write(tempBuffer, bytesRead);
                    }
                    if (bytesRead == -1) {
                      fileSink.flush();
                      fileSink.close();
                      final long fileLen = cacheFile.length();
                      EXECUTOR.execute(
                          () ->
                              processCachedImageAndReport(
                                  cacheFile,
                                  fileLen,
                                  response,
                                  plugin,
                                  requestId,
                                  isMock,
                                  requestStartMs,
                                  responseReceivedMs,
                                  factory));
                    }
                    return bytesRead;
                  }
                });
          }
        };

    return response.newBuilder().body(wrappedBody).build();
  }

  private static void processCachedImageAndReport(
      File cacheFile,
      long fileLen,
      Response response,
      NetworkFlipperPlugin plugin,
      String requestId,
      boolean isMock,
      long requestStartMs,
      long responseReceivedMs,
      ResponseInfoFactory factory) {
    try {
      if (fileLen <= 0 || !cacheFile.isFile()) {
        return;
      }
      final boolean useThumbnail = fileLen > THUMBNAIL_IF_FILE_LARGER_THAN_BYTES;
      byte[] bodyForFlipper;
      if (useThumbnail) {
        bodyForFlipper = buildThumbnailJpeg(cacheFile);
      } else {
        bodyForFlipper = readFileFully(cacheFile);
      }
      List<Header> exifHeaders = readExifAsFlipperHeaders(cacheFile);
      ResponseInfo info =
          factory.create(
              response, bodyForFlipper, requestId, isMock, requestStartMs, responseReceivedMs);
      if (bodyForFlipper != null && bodyForFlipper.length > 0) {
        if (useThumbnail) {
          info.headers.add(
              new Header("flipper-img-preview-short-side", String.valueOf(THUMBNAIL_SHORT_SIDE)));
          info.headers.add(
              new Header(
                  "flipper-img-preview-jpeg-quality", String.valueOf(THUMBNAIL_JPEG_QUALITY)));
          info.headers.add(new Header("flipper-img-response-body-mime", "image/jpeg"));
          info.headers.add(new Header("flipper-img-body-source", "thumbnail"));
        } else {
          String ct = response.header("Content-Type");
          if (!TextUtils.isEmpty(ct)) {
            info.headers.add(new Header("flipper-img-response-body-mime", ct));
          }
          info.headers.add(new Header("flipper-img-body-source", "original"));
        }
      }
      for (Header h : exifHeaders) {
        info.headers.add(h);
      }
      plugin.reportResponse(info);
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      //noinspection ResultOfMethodCallIgnored
      cacheFile.delete();
    }
  }

  private static byte[] readFileFully(File file) {
    try {
      return Okio.buffer(Okio.source(file)).readByteArray();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  private static byte[] buildThumbnailJpeg(File imageFile) {
    BitmapFactory.Options bounds = new BitmapFactory.Options();
    bounds.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bounds);
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
      return null;
    }
    int shortSide = Math.min(bounds.outWidth, bounds.outHeight);
    int inSampleSize = 1;
    while (shortSide / (inSampleSize * 2) >= THUMBNAIL_SHORT_SIDE) {
      inSampleSize *= 2;
    }

    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inSampleSize = inSampleSize;
    Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
    if (bmp == null) {
      return null;
    }
    try {
      int w = bmp.getWidth();
      int h = bmp.getHeight();
      int ss = Math.min(w, h);
      if (ss > THUMBNAIL_SHORT_SIDE) {
        float scale = THUMBNAIL_SHORT_SIDE / (float) ss;
        int nw = Math.max(1, Math.round(w * scale));
        int nh = Math.max(1, Math.round(h * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, nw, nh, true);
        if (scaled == null) {
          return null;
        }
        if (scaled != bmp) {
          bmp.recycle();
        }
        bmp = scaled;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      if (!bmp.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, baos)) {
        return null;
      }
      return baos.toByteArray();
    } finally {
      if (bmp != null && !bmp.isRecycled()) {
        bmp.recycle();
      }
    }
  }

  private static final String[] EXIF_TAGS_TO_COPY =
      new String[] {
        ExifInterface.TAG_IMAGE_WIDTH,
        ExifInterface.TAG_IMAGE_LENGTH,
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_SOFTWARE,
        ExifInterface.TAG_ARTIST,
        ExifInterface.TAG_COPYRIGHT,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
        ExifInterface.TAG_ISO_SPEED_RATINGS,
        ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_WHITE_BALANCE,
        ExifInterface.TAG_COLOR_SPACE,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_IMAGE_DESCRIPTION,
        ExifInterface.TAG_USER_COMMENT,
      };

  private static List<Header> readExifAsFlipperHeaders(File imageFile) {
    List<Header> out = new ArrayList<>();
    ExifInterface exif;
    try {
      exif = new ExifInterface(imageFile.getAbsolutePath());
    } catch (IOException e) {
      return out;
    }
    for (String tag : EXIF_TAGS_TO_COPY) {
      String v = exif.getAttribute(tag);
      if (!TextUtils.isEmpty(v)) {
        out.add(new Header("flipper-img-" + tag.replace('/', '-'), v));
      }
    }
    float[] latLong = new float[2];
    if (exif.getLatLong(latLong)) {
      out.add(new Header("flipper-img-GPSLatitude-decimal", String.valueOf(latLong[0])));
      out.add(new Header("flipper-img-GPSLongitude-decimal", String.valueOf(latLong[1])));
    }
   /* double[] alt = new double[1];
    if (exif.getAltitude(alt)) {
      out.add(new Header("flipper-img-GPSAltitude-meters", String.valueOf(alt[0])));
    }*/
    return out;
  }
}
