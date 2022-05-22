package com.example.taskboard.component;

import com.vaadin.flow.component.Key;
import com.example.taskboard.model.Task;
import com.example.taskboard.repository.TaskRepository;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

@SpringComponent
@UIScope
public class TaskEditor extends VerticalLayout implements KeyNotifier {

    private final TaskRepository taskRepository;

    private Task task;

    /* Field to edit properties in Task entity */
    private final TextField type = new TextField("Task type");
    private final TextField inputData = new TextField("Input Data");

    /* Action buttons*/
    private final Button save = new Button("Save", VaadinIcon.CHECK.create());
    private final Button cancel = new Button("Cancel");
    private final Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    private final HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    private final Binder<Task> binder;
    @Setter
    private ChangeHandler changeHandler;

    @Autowired
    public TaskEditor(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.binder = new Binder<>(Task.class);

        inputData.setWidth("1000");
        add(type, inputData, save, cancel, delete);
        type.setWidth("1000");


        binder.bindInstanceFields(this);

        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editTask(task));
        setVisible(false);
    }

    private void delete() {
        taskRepository.delete(task);
        changeHandler.onChange();
    }

    private void save() {
        taskRepository.save(task);
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
}
