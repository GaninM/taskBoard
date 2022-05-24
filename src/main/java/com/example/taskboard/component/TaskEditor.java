package com.example.taskboard.component;

import com.vaadin.flow.component.Key;
import com.example.taskboard.model.Task;
import com.example.taskboard.repository.TaskRepository;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringComponent
@UIScope
public class TaskEditor extends VerticalLayout implements KeyNotifier {

    private final TaskRepository taskRepository;
    private Task task;

    private final ComboBox<String> comboBox = new ComboBox<>("Task type");
    private final TextField inputData = new TextField("Input Data for substrings");
    TextField inputDataForStrings = new TextField("Input Data For Strings");
    private final TextField[][] inputDataForSquareTask = new TextField[3][3];

    private final Button save = new Button("Save", VaadinIcon.CHECK.create());
    private final Button export = new Button("Export", VaadinIcon.CHECK.create());
    private final Button importing = new Button("Import", VaadinIcon.CHECK.create());
    private final Button cancel = new Button("Cancel");
    private final Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    private final HorizontalLayout actions = new HorizontalLayout(save, export, importing, cancel, delete);

    private final Binder<Task> binder;
    @Setter
    private ChangeHandler changeHandler;

    @Autowired
    public TaskEditor(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.binder = new Binder<>(Task.class);

        List<String> taskTypes = new ArrayList<>();
        taskTypes.add("Substrings");
        taskTypes.add("Magic square");
        comboBox.setItems(taskTypes);
        comboBox.addValueChangeListener(e -> showField(e.getValue()));


        VerticalLayout verticalLayoutForSubstringTask = new VerticalLayout();

        inputDataForStrings.setWidth("1000px");
        inputData.setVisible(false);
        inputDataForStrings.setVisible(false);
        verticalLayoutForSubstringTask.add(inputData, inputDataForStrings);
        add(comboBox, verticalLayoutForSubstringTask);
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

        add(actions);

        inputData.setWidth("1000px");

        binder.bindInstanceFields(this);

        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        save.addClickListener(e -> save());
        export.addClickListener(e -> {
            try {
                export();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        importing.addClickListener(e -> importing());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> cancel());
        setVisible(false);
    }

    private void importing() {
    }

    private void export() throws FileNotFoundException {
        String absolutePath = new File("").getAbsolutePath();
                FileOutputStream out = new FileOutputStream(absolutePath +
                "//src//main//resources//taskDirectory//" + "FileType=" + comboBox.getValue() + "InputData=" +
                inputData.getValue() + ".txt");
        changeHandler.onChange();
    }

    private void delete() {
        taskRepository.delete(task);
        changeHandler.onChange();
    }

    private void save() {
        task.setType(comboBox.getValue());
        taskRepository.save(task);
        changeHandler.onChange();
    }

    private void cancel() {
        setVisible(false);
        changeHandler.onChange();
    }

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

        inputData.focus();
    }

    private void showField(String type) {
        if (type.equals("Magic square")) {
            inputData.setVisible(false);
            inputDataForStrings.setVisible(false);
            showInputDataForSquareTask();
        } else if (type.equals("Substrings")) {
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

    private void hideInputDataForSquareTask() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                inputDataForSquareTask[i][j].setVisible(false);
            }
        }
    }


}
