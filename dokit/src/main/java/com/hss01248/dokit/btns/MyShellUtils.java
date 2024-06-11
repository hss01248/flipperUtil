package com.hss01248.dokit.btns;

import android.os.Build.VERSION;

import com.blankj.utilcode.util.LogUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class MyShellUtils {
    private static final String LINE_SEP = System.getProperty("line.separator");

    private MyShellUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static CommandResult execCmd(String command, boolean isRooted) {
        return execCmd(new String[]{command}, isRooted, true);
    }

    public static CommandResult execCmd(String command, List<String> envp, boolean isRooted) {
        return execCmd(new String[]{command}, envp == null ? null : (String[])envp.toArray(new String[0]), isRooted, true);
    }

    public static CommandResult execCmd(List<String> commands, boolean isRooted) {
        return execCmd(commands == null ? null : (String[])commands.toArray(new String[0]), isRooted, true);
    }

    public static CommandResult execCmd(List<String> commands, List<String> envp, boolean isRooted) {
        return execCmd(commands == null ? null : (String[])commands.toArray(new String[0]), envp == null ? null : (String[])envp.toArray(new String[0]), isRooted, true);
    }

    public static CommandResult execCmd(String[] commands, boolean isRooted) {
        return execCmd(commands, isRooted, true);
    }

    public static CommandResult execCmd(String command, boolean isRooted, boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, isRooted, isNeedResultMsg);
    }

    public static CommandResult execCmd(String command, List<String> envp, boolean isRooted, boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, envp == null ? null : (String[])envp.toArray(new String[0]), isRooted, isNeedResultMsg);
    }

    public static CommandResult execCmd(String command, String[] envp, boolean isRooted, boolean isNeedResultMsg) {
        return execCmd(new String[]{command}, envp, isRooted, isNeedResultMsg);
    }

    public static CommandResult execCmd(List<String> commands, boolean isRooted, boolean isNeedResultMsg) {
        return execCmd(commands == null ? null : (String[])commands.toArray(new String[0]), isRooted, isNeedResultMsg);
    }

    public static CommandResult execCmd(String[] commands, boolean isRooted, boolean isNeedResultMsg) {
        return execCmd((String[])commands, (String[])null, isRooted, isNeedResultMsg);
    }

    public static CommandResult execCmd(String[] commands, String[] envp, boolean isRooted, boolean isNeedResultMsg) {
        int result = -1;
        if (commands != null && commands.length != 0) {
            Process process = null;
            BufferedReader successResult = null;
            BufferedReader errorResult = null;
            StringBuilder successMsg = null;
            StringBuilder errorMsg = null;
            DataOutputStream os = null;

            try {
                process = Runtime.getRuntime().exec(isRooted ? "su" : "sh", envp, (File)null);
                os = new DataOutputStream(process.getOutputStream());
                String[] var11 = commands;
                int var12 = commands.length;

                for(int var13 = 0; var13 < var12; ++var13) {
                    String command = var11[var13];
                    if (command != null) {
                        os.write(command.getBytes());
                        os.writeBytes(LINE_SEP);
                        os.flush();
                    }
                }

                os.writeBytes("exit" + LINE_SEP);
                os.flush();
                if (VERSION.SDK_INT >= 26) {
                    result = process.waitFor(1L, TimeUnit.SECONDS) ? 1 : 0;
                } else {
                    result = process.waitFor();
                }

                if (isNeedResultMsg) {
                    successMsg = new StringBuilder();
                    errorMsg = new StringBuilder();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"));
                    String line;
                    if ((line = successResult.readLine()) != null) {
                        successMsg.append(line);

                        while((line = successResult.readLine()) != null) {
                            successMsg.append(LINE_SEP).append(line);
                        }
                    }

                    if ((line = errorResult.readLine()) != null) {
                        errorMsg.append(line);

                        while((line = errorResult.readLine()) != null) {
                            errorMsg.append(LINE_SEP).append(line);
                        }
                    }
                }
            } catch (Exception var31) {
                LogUtils.w(var31);
            } finally {
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException var30) {
                    var30.printStackTrace();
                }

                try {
                    if (successResult != null) {
                        successResult.close();
                    }
                } catch (IOException var29) {
                    var29.printStackTrace();
                }

                try {
                    if (errorResult != null) {
                        errorResult.close();
                    }
                } catch (IOException var28) {
                    var28.printStackTrace();
                }

                if (process != null) {
                    process.destroy();
                }

            }

            return new CommandResult(result, successMsg == null ? "" : successMsg.toString(), errorMsg == null ? "" : errorMsg.toString());
        } else {
            return new CommandResult(result, "", "");
        }
    }

    public static class CommandResult {
        public int result;
        public String successMsg;
        public String errorMsg;

        public CommandResult(int result, String successMsg, String errorMsg) {
            this.result = result;
            this.successMsg = successMsg;
            this.errorMsg = errorMsg;
        }

        public String toString() {
            return "result: " + this.result + "\nsuccessMsg: " + this.successMsg + "\nerrorMsg: " + this.errorMsg;
        }
    }
}

