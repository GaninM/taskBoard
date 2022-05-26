package com.example.taskboard.component;

import com.example.taskboard.controllers.FileController;
import com.vaadin.flow.component.Key;
import com.example.taskboard.model.Task;
import com.example.taskboard.repository.TaskRepository;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;

@SpringComponent
@UIScope
public class TaskEditor extends VerticalLayout implements KeyNotifier {

    private final TaskRepository taskRepository;
    private final FileController fileController;
    private Task task;
    @Getter
    private final ComboBox<String> comboBox;
    @Getter
    private final TextField inputData;
    @Getter
    private final TextField inputDataForStrings;
    @Getter
    private final TextField[][] inputDataForSquareTask;
    @Getter
    private final Button save;
    @Getter
    private final Button export;
    @Getter
    private final Button calculate;
    @Getter
    private final Button cancel;
    @Getter
    private final Button delete;
    private final MemoryBuffer buffer;
    @Getter
    private final Upload upload;
    @Getter
    private final TextArea resultArea;
    private final Binder<Task> binder;
    @Setter
    private ChangeHandler changeHandler;

    @Autowired
    public TaskEditor(TaskRepository taskRepository, FileController fileController) {
        this.taskRepository = taskRepository;
        this.fileController = fileController;
        this.binder = new Binder<>(Task.class);
        this.comboBox = new ComboBox<>("Task type");
        this.inputData = new TextField("Input Data for substrings");
        this.inputDataForStrings = new TextField("Input Data For Strings");
        this.inputDataForSquareTask = new TextField[3][3];
        this.save = new Button("Save", VaadinIcon.CHECK.create());
        this.export = new Button("Export", VaadinIcon.CHECK.create());
        this.calculate = new Button("Calculate", VaadinIcon.CHECK.create());
        this.cancel = new Button("Cancel");
        this.delete = new Button("Delete", VaadinIcon.TRASH.create());
        this.buffer = new MemoryBuffer();
        this.upload = new Upload(buffer);
        this.resultArea = new TextArea();
        HorizontalLayout resultLayout = new HorizontalLayout(calculate, resultArea);
        HorizontalLayout actions = new HorizontalLayout(save, export, cancel, delete);


        upload.setDropAllowed(true);
        upload.setWidth("500px");
        upload.setAcceptedFileTypes(".txt");
        upload.setMaxFiles(1);
        upload.setVisible(true);

        resultArea.setReadOnly(true);
        resultArea.setWidth("400px");
        resultArea.setHeight("300px");
        upload.addSucceededListener(e -> {
            Task taskFromUpload = null;
            InputStream fileData = buffer.getInputStream();
            String fileName = e.getFileName();
            try {
                taskFromUpload = fileController.importing(fileData, fileName);
            } catch (IOException ex) {
                ex.getStackTrace();
            }
            calculate(Objects.requireNonNull(taskFromUpload));
            resultArea.setVisible(true);
            calculate.setVisible(true);
        });


        List<String> taskTypes = new ArrayList<>();
        taskTypes.add("Substrings");
        taskTypes.add("Magic square");
        comboBox.setItems(taskTypes);
        comboBox.addValueChangeListener(e -> showField(e.getValue()));

        VerticalLayout verticalLayoutForSubstringTask = new VerticalLayout();
        inputDataForStrings.setWidth("1000px");
        inputData.setWidth("1000px");
        inputData.setVisible(false);
        inputDataForStrings.setVisible(false);
        verticalLayoutForSubstringTask.add(inputData, inputDataForStrings);

        //initial table for squareTask
        for (int i = 0; i < 3; i++) {
            HorizontalLayout horizontalLayout = new HorizontalLayout();
            for (int j = 0; j < 3; j++) {
                inputDataForSquareTask[i][j] = new TextField();
                inputDataForSquareTask[i][j].setWidth("50px");
                inputDataForSquareTask[i][j].addThemeVariants(TextFieldVariant.LUMO_SMALL);
                inputDataForSquareTask[i][j].setLabel("[" + i + "][" + j + "]");
                inputDataForSquareTask[i][j].setVisible(false);
                horizontalLayout.add(inputDataForSquareTask[i][j]);
            }
            add(horizontalLayout);
        }

        resultLayout.setVisible(true);
        add(resultLayout, comboBox, verticalLayoutForSubstringTask, actions);

        binder.bindInstanceFields(this);

        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        save.addClickListener(e -> save());
        export.addClickListener(e -> fileController.export(comboBox.getValue(), comboBox, inputDataForSquareTask,
                inputData, inputDataForStrings));
        cancel.addClickListener(e -> cancel());
        calculate.addClickListener(e -> calculate(task));
        delete.addClickListener(e -> delete());
        setVisible(false);
    }

    private void save() {
        task.setType(comboBox.getValue());
        task.setInputData(fileController.parseTask(comboBox.getValue(), inputDataForSquareTask,
                inputData, inputDataForStrings));
        taskRepository.save(task);
        changeHandler.onChange();
        comboBox.setValue("");
    }

    private void cancel() {
        setVisible(false);
        upload.setVisible(true);
        resultArea.setValue("");
        resultArea.setVisible(false);
        changeHandler.onChange();
        comboBox.setValue("");
    }

    private void delete() {
        taskRepository.deleteById(task.getId());
        changeHandler.onChange();
    }

    private void calculate(Task task) {
        //task = taskRepository.findById(task.getId()).orElse(task);
        if (task.getType().equalsIgnoreCase("Magic square")) {
            String[] allData = task.getInputData().split("}\\{");
            for (int i = 0; i < allData.length; i++) {
                String tmp = allData[i];
                if (tmp.contains("}")) {
                    tmp = allData[i].replaceAll("}", "");
                }
                if (tmp.contains("{")) {
                    tmp = allData[i].replaceAll("\\{", "");
                }
                allData[i] = tmp;
            }
            int[][] resultArr = new int[3][3];
            for (int i = 0; i < allData.length; i++) {
                if (allData[i].endsWith(".")) {
                    allData[i] = allData[i].substring(0, allData[i].length() - 1);
                }
                for (int j = 0; j < allData.length; j++) {
                    String[] tmp = allData[i].split("\\.");
                    resultArr[i][j] = Integer.parseInt(tmp[j]);
                }
            }
            findMinimum(resultArr);
        } else if (task.getType().equalsIgnoreCase("Substrings")) {
            String[] allData = task.getInputData().split(",");
            String[] substrings = allData[0].split(" ");
            String[] strings = allData[1].split(" ");

            //Skip first element if space
            if (allData[1].charAt(0) == '\u0020') {
                strings = Arrays.stream(strings).skip(1).toArray(String[]::new);
            }

            inArray(substrings, strings);
            resultArea.setValue("Первый массив: " + "\n" + Arrays.toString(substrings) + "\n\n" +
                    "Второй массив: " + "\n" + Arrays.toString(strings) + "\n\n" +
                    "Отсортированные строки из первого массива, которые входят как подстроки во второй: " + "\n" +
                    Arrays.toString(inArray(substrings, strings)));
            resultArea.setVisible(true);
        }
    }

    //Method creating window for edit task or create task
    public void editTask(Task newTask) {
        if (newTask == null) {
            setVisible(false);
            return;
        }
        if (newTask.getId() != null) {
            task = taskRepository.findById(newTask.getId()).orElse(newTask);
        } else {
            task = newTask;
        }
        binder.setBean(task);
        setVisible(true);
    }

    //Method showing task depending on type task
    private void showField(String type) {
        if (type.equalsIgnoreCase("Magic square")) {
            inputData.setVisible(false);
            inputDataForStrings.setVisible(false);
            showInputDataForSquareTask();
        } else if (type.equalsIgnoreCase("Substrings")) {
            inputData.setVisible(true);
            inputDataForStrings.setVisible(true);
            hideInputDataForSquareTask();
        } else {
            inputData.setVisible(false);
            inputDataForStrings.setVisible(false);
            hideInputDataForSquareTask();
        }
    }

    private void showInputDataForSquareTask() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inputDataForSquareTask[i][j].setVisible(true);
            }
        }
    }

    public void hideInputDataForSquareTask() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inputDataForSquareTask[i][j].setVisible(false);
            }
        }
    }

    private String[] inArray(String[] substringsArray, String[] stringsArray) {
        String[] substringsInStringsArray = new String[substringsArray.length];
        int count = 0;
        for (String substring : substringsArray) {
            for (String string : stringsArray) {
                if (string.contains(substring)) {
                    substringsInStringsArray[count++] = substring;
                    break;
                }
            }
        }
        if (count < substringsInStringsArray.length) {
            String[] tmp = new String[count];
            System.arraycopy(substringsInStringsArray, 0, tmp, 0, count);
            substringsInStringsArray = tmp;
        }
        Arrays.sort(substringsInStringsArray);
        substringsInStringsArray = Arrays.stream(substringsInStringsArray).distinct().toArray(String[]::new);
        return substringsInStringsArray;
    }

    private int[] findMinimumFromMS(int[][] arr, int[][] ms) {
        int[] returnData = new int[2];
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (arr[i][j] != ms[i][j])
                    returnData[0] = count++;
                returnData[1] += Math.abs(arr[i][j] - ms[i][j]);
            }
        }
        return returnData;
    }

    private void findMinimum(int[][] arr) {
        int[][][] ms = {
                {{8, 1, 6}, {3, 5, 7}, {4, 9, 2}},
                {{6, 1, 8}, {7, 5, 3}, {2, 9, 4}},
                {{4, 9, 2}, {3, 5, 7}, {8, 1, 6}},
                {{2, 9, 4}, {7, 5, 3}, {6, 1, 8}},
                {{8, 3, 4}, {1, 5, 9}, {6, 7, 2}},
                {{4, 3, 8}, {9, 5, 1}, {2, 7, 6}},
                {{6, 7, 2}, {1, 5, 9}, {8, 3, 4}},
                {{2, 7, 6}, {9, 5, 1}, {4, 3, 8}},
        };

        int min = 9;
        int sum = 0;
        int[][] newArr = new int[3][3];
        if (isMagicSquare(arr)) {
            resultArea.setValue("Данный массив является магическим квадратом: " + "\n" + printArray(arr));
            resultArea.setVisible(true);
        } else {
            for (int i = 0; i < 8; i++) {
                int x = findMinimumFromMS(arr, ms[i])[0];
                if (x < min) {
                    min = x;
                    sum = findMinimumFromMS(arr, ms[i])[1];
                    newArr = ms[i];
                }
            }
            resultArea.setValue("Первоночальный массив: " + "\n" + printArray(arr) + "\n" +
                    "Новый массив: " + "\n" + printArray(newArr) + "\n" +
                    "Колличество замененных чисел: " + min + "\n" +
                    "Сумма замененых числе " + sum);
            resultArea.setVisible(true);
        }
    }

    private String printArray(int[][] arr) {
        StringBuilder arrayText = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                arrayText.append(arr[i][j]).append("\t");
            }
            arrayText.append("\n");
        }
        return String.valueOf(arrayText);
    }

    static boolean isMagicSquare(int[][] mat) {
        // sumd1 and sumd2 are the sum of the two diagonals
        int sumd1 = 0;
        int sumd2 = 0;
        for (int i = 0; i < 3; i++) {
            // (i, i) is the diagonal from top-left -> bottom-right
            // (i, N - i - 1) is the diagonal from top-right -> bottom-left
            sumd1 += mat[i][i];
            sumd2 += mat[i][3 - 1 - i];
        }
        // if the two diagonal sums are unequal then it is not a magic square
        if (sumd1 != sumd2) {
            return false;
        }

        // calculating sums of Rows and columns and checking if they are equal to each other,
        // as well as equal to diagonal sum or not
        for (int i = 0; i < 3; i++) {

            int rowSum = 0;
            int colSum = 0;
            for (int j = 0; j < 3; j++) {
                rowSum += mat[i][j];
                colSum += mat[j][i];
            }
            if (rowSum != colSum || colSum != sumd1) {
                return false;
            }
        }
        return true;
    }
}


