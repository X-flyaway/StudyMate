package main.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStorage {
    private static final Path FILE_PATH =
            Path.of("data", "studymate.dat");

    public static void save(StudyPlan plan) throws IOException {
        Files.createDirectories(FILE_PATH.getParent());

        try (ObjectOutputStream out =
                     new ObjectOutputStream(
                             Files.newOutputStream(FILE_PATH))) {
            out.writeObject(plan);
        }
    }

    public static StudyPlan load() throws IOException {
        if (!Files.exists(FILE_PATH)) {
            return null;
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(
                             Files.newInputStream(FILE_PATH))) {
            Object object = in.readObject();

            if (object instanceof StudyPlan plan) {
                return plan;
            }

            throw new IOException("存档文件的类型不正确。");
        } catch (ClassNotFoundException e) {
            throw new IOException("无法读取学习计划的数据类型。", e);
        }
    }
}
