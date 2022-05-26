package com.example.taskboard.controllers;

import com.example.taskboard.component.ChangeHandler;
import com.example.taskboard.model.Task;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Component
public class FileController {

    @Setter
    ChangeHandler changeHandler;

    public void export(String type, ComboBox comboBox, TextField[][] inputDataForSquareTask, TextField inputData,
                       TextField inputDataForStrings) {
        String absolutePath = new File("").getAbsolutePath();
        Path path = null;
        if (type.equalsIgnoreCase("Magic square")) {
            File out = new File(absolutePath + "//src//main//resources//taskDirectory//" +
                    "FileType=" + comboBox.getValue() + "InputData=" +
                    parseTask(type,inputDataForSquareTask,inputData,inputDataForStrings ) + ".txt");
            path = Path.of(out.getPath());
        } else if (type.equalsIgnoreCase("Substrings")) {
            File out = new File(absolutePath + "//src//main//resources//taskDirectory//" +
                    "FileType=" + comboBox.getValue() + "InputData=" + inputData.getValue() + ".txt");
            path = Path.of(out.getPath());
        }

        try {
            Files.writeString(Objects.requireNonNull(path), parseTask((String) comboBox.getValue(),
                    inputDataForSquareTask, inputData, inputDataForStrings), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            ioException.getStackTrace();
        }
        changeHandler.onChange();
    }

    public Task importing(InputStream fileData, String fileName) throws IOException {
        Task newTask = new Task();
        newTask.setId(32L);
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(fileData, StandardCharsets.UTF_8);

        for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
            out.append(buffer, 0, numRead);
        }
        String typeTask = fileName.substring(fileName.indexOf("=") + 1, fileName.indexOf("Input"));
        newTask.setInputData(out.toString());
        newTask.setType(typeTask);
        return newTask;
    }

    public String parseTask(String type, TextField[][] inputDataForSquareTask, TextField inputData,
                            TextField inputDataForStrings) {
        if (type.equals("Magic square")) {
            StringBuilder magicSquareData = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                magicSquareData.append("{");
                for (int j = 0; j < 3; j++) {
                    magicSquareData.append(inputDataForSquareTask[i][j].getValue()).append(".");
                }
                magicSquareData.append("}");
            }
            return magicSquareData.toString();
        } else if (type.equals("Substrings")) {
            return inputData.getValue() + "," + inputDataForStrings.getValue();
        }
        return "inputDataIsEmpty";
    }
}
