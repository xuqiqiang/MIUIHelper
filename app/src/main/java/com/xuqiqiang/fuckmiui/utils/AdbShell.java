package com.xuqiqiang.fuckmiui.utils;

import android.os.Build;
import android.util.Pair;
import androidx.annotation.Nullable;
import com.xuqiqiang.fuckmiui.BuildConfig;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuRemoteProcess;

public class AdbShell implements Shell {
    private static final Pattern sessionIdPattern = Pattern.compile("(\\d+)");

    @Override
    public Result exec(Command command) {
        return exec(command, null);
    }

    @Override
    public Result exec(Command command, @Nullable InputStream inputPipe) {
        StringBuilder stdOutSb = new StringBuilder();
        StringBuilder stdErrSb = new StringBuilder();

        try {
            Shell.Command.Builder shCommand =
                new Shell.Command.Builder("sh", "-c", command.toString());
            //RemoteProcess process = Shizuku.newProcess(shCommand.build().toStringArray(), null, null);
            ShizukuRemoteProcess process =
                Shizuku.newProcess(shCommand.build().toStringArray(), null, null);

            Thread stdOutD = IOUtils.writeStreamToStringBuilder(stdOutSb, process.getInputStream());
            Thread stdErrD = IOUtils.writeStreamToStringBuilder(stdErrSb, process.getErrorStream());

            if (inputPipe != null) {
                try (OutputStream outputStream = process.getOutputStream();
                     InputStream inputStream = inputPipe) {
                    IOUtils.copy(inputStream, outputStream);
                } catch (Exception e) {
                    stdOutD.interrupt();
                    stdErrD.interrupt();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        process.destroyForcibly();
                    } else {
                        process.destroy();
                    }

                    throw new RuntimeException(e);
                }
            }

            process.waitFor();
            stdOutD.join();
            stdErrD.join();

            return new Shell.Result(command, process.exitValue(), stdOutSb.toString().trim(),
                stdErrSb.toString().trim());
        } catch (Exception e) {
            L.e("Unable execute command: " + e);
            e.printStackTrace();
            return new Shell.Result(command, -1, stdOutSb.toString().trim(),
                stdErrSb + "\n\n<!> SAI ShizukuShell Java exception: ");
        }
    }

    private Integer extractSessionId(String commandResult) {
        try {
            Matcher sessionIdMatcher = sessionIdPattern.matcher(commandResult);
            sessionIdMatcher.find();
            return Integer.parseInt(sessionIdMatcher.group(1));
        } catch (Exception e) {
            L.e("extractSessionId", commandResult, e);
            return null;
        }
    }

    @Override
    public String makeLiteral(String arg) {
        return "'" + arg.replace("'", "'\\''") + "'";
    }

    public int createSession() {
        ArrayList<Command> commandsToAttempt = new ArrayList<>();
        // "com.miui.packageinstaller"
        commandsToAttempt.add(
            new Shell.Command("pm", "install-create", "-r", "-d", "--user 0", "--install-location",
                "0", "-i", makeLiteral("")));
        commandsToAttempt.add(
            new Shell.Command("pm", "install-create", "-r", "-d", "-- user 0", "-i",
                makeLiteral(BuildConfig.APPLICATION_ID)));
                //makeLiteral(mContext.getPackageName())));

        List<Pair<Command, String>> attemptedCommands = new ArrayList<>();

        for (Shell.Command commandToAttempt : commandsToAttempt) {
            Shell.Result result = exec(commandToAttempt);
            attemptedCommands.add(new Pair<>(commandToAttempt, result.toString()));

            if (!result.isSuccessful()) {
                L.e(String.format("Command error: %s > %s", commandToAttempt, result));
                continue;
            }

            Integer sessionId = extractSessionId(result.out);
            if (sessionId != null) {
                return sessionId;
            } else {
                L.e(String.format("Command error: %s > %s", commandToAttempt, result));
            }
        }

        StringBuilder exceptionMessage =
            new StringBuilder("Unable to create session, attempted commands: ");
        int i = 1;
        for (Pair<Shell.Command, String> attemptedCommand : attemptedCommands) {
            exceptionMessage.append("\n\n").append(i++).append(") ==========================\n")
                .append(attemptedCommand.first)
                .append("\nVVVVVVVVVVVVVVVV\n")
                .append(attemptedCommand.second);
        }
        exceptionMessage.append("\n");

        L.e("createSession error", exceptionMessage);
        return -1;
    }
}
