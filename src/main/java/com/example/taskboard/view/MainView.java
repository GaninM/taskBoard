package com.example.taskboard.view;

import com.example.taskboard.component.TaskEditor;
import com.example.taskboard.model.Task;
import com.example.taskboard.repository.TaskRepository;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route("")
public class MainView extends VerticalLayout {
    private final transient TaskRepository taskRepository;

    private final TextField filter;
    private final Button addNewBtn;
    private final HorizontalLayout toolbar;

    private final TaskEditor editor;

    private final Grid<Task> grid;

    @Autowired
    public MainView(TaskRepository taskRepository, TaskEditor editor) {
        this.taskRepository = taskRepository;
        this.editor = editor;
        this.grid = new Grid<>(Task.class);
        this.filter = new TextField("", "Type to filter");
        this.addNewBtn = new Button("Add new");
        this.toolbar = new HorizontalLayout(filter, addNewBtn);

        add(toolbar, grid, editor, editor.getUpload());

        //Creating filter settings
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> showTask(e.getValue()));

        //Added function for edit table row on click
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editTask(e.getValue());
            editor.getComboBox().setVisible(false);
            editor.getInputData().setVisible(false);
            editor.getInputDataForStrings().setVisible(false);
            editor.hideInputDataForSquareTask();
            editor.getSave().setVisible(false);
            editor.getExport().setVisible(false);
            editor.getImporting().setVisible(false);
            editor.getUpload().setVisible(false);
        });

        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            showTask(filter.getValue());
        });

        addNewBtn.addClickListener(e -> {
            editor.editTask(new Task());
            editor.getComboBox().setVisible(true);
            editor.getSave().setVisible(true);
            editor.getExport().setVisible(true);
            editor.getImporting().setVisible(true);
            editor.getUpload().setVisible(true);
        });

        showTask("");
    }

    private void showTask(String type) {
        if (type.isEmpty()) {
            grid.setItems(taskRepository.findAll());
        } else {
            grid.setItems(taskRepository.findByType(type));
        }
    }
}
