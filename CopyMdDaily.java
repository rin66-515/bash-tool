import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CopyMdDaily {

    private static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("使い方: java CopyMdDaily <ソースフォルダのパス> <ターゲットフォルダのパス>");
            System.out.println("例: java CopyMdDaily C:\\source_md_folders C:\\my_project\\post");
            return;
        }

        String sourceRoot = args[0];
        String targetFolder = args[1];

        File logFile = new File("copy_md_log.txt");

        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile, true))) {
            logWriter.write("===== タスク開始: " + java.time.LocalDateTime.now() + " =====\n");
            copyMdFiles(sourceRoot, targetFolder, logWriter);
            logWriter.write("===== タスク終了 =====\n\n");
        } catch (IOException e) {
            System.err.println("ログファイルの書き込みエラー: " + e.getMessage());
        }
    }

    private static void copyMdFiles(String sourceRoot, String targetFolder, BufferedWriter logWriter) {
        File sourceDir = new File(sourceRoot);
        File[] files = sourceDir.listFiles();
        if (files == null) {
            writeLog(logWriter, "ソースフォルダが存在しないかアクセスできません: " + sourceRoot);
            return;
        }

        writeLog(logWriter, "ソースフォルダ直下のファイル・フォルダ一覧:");
        for (File f : files) {
            writeLog(logWriter, "  " + f.getName() + (f.isDirectory() ? " (フォルダ)" : " (ファイル)"));
        }

        File targetDir = new File(targetFolder);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (created) {
                writeLog(logWriter, "ターゲットフォルダを作成しました: " + targetFolder);
            } else {
                writeLog(logWriter, "ターゲットフォルダの作成に失敗しました: " + targetFolder);
                return;
            }
        }

        LocalDate today = LocalDate.now();

        for (File dateFolder : files) {
            if (!dateFolder.isDirectory()) {
                continue;
            }

            String folderName = dateFolder.getName();
            LocalDate folderDate;
            try {
                folderDate = LocalDate.parse(folderName, DATE_FORMATTER);
            } catch (Exception e) {
                writeLog(logWriter, "フォルダ名が日付形式ではありません。スキップします: " + folderName);
                continue;
            }

            // 未来の日付フォルダをスキップ
            if (folderDate.isAfter(today)) {
                writeLog(logWriter, "未来の日付フォルダをスキップします: " + folderName);
                continue;
            }

            File[] mdFiles = dateFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".md"));
            if (mdFiles == null || mdFiles.length == 0) {
                writeLog(logWriter, "日付フォルダに.mdファイルがありません: " + folderName);
                continue;
            }

            for (File srcFile : mdFiles) {
                File targetFile = new File(targetDir, srcFile.getName());
                try {
                    if (targetFile.exists()) {
                        long srcModified = srcFile.lastModified();
                        long targetModified = targetFile.lastModified();
                        if (srcModified > targetModified) {
                            Files.copy(srcFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            writeLog(logWriter, "ファイルを上書き更新しました: " + srcFile.getName() + " (フォルダ: " + folderName + ")");
                        } else {
                            writeLog(logWriter, "古いファイルのためスキップしました: " + srcFile.getName());
                        }
                    } else {
                        Files.copy(srcFile.toPath(), targetFile.toPath());
                        writeLog(logWriter, "新規ファイルをコピーしました: " + srcFile.getName() + " (フォルダ: " + folderName + ")");
                    }
                } catch (IOException e) {
                    writeLog(logWriter, "ファイルコピーエラー " + srcFile.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    private static void writeLog(BufferedWriter logWriter, String message) {
        try {
            System.out.println(message);
            logWriter.write(java.time.LocalDateTime.now() + " - " + message + "\n");
            logWriter.flush();
        } catch (IOException e) {
            System.err.println("ログ書き込み失敗: " + e.getMessage());
        }
    }
}