# todo-mvc

[TodoMVC](https://github.com/addyosmani/todomvc) implementation in Pedestal.

## Remaining Spec features:

* When there are no todos, #main and #footer should be hidden.
* The app should dynamically persist the todos to localStorage.
    * Use the keys id, title, completed for each item.
    * localStorage name shoudl be: todos-spoke.
    * Editing mode should not be persisted.
* There should be three routes:
    * / - default
    * /active - all active todos
    * /completed - all completed todos
* [X] New todos...
    * [X] Enter creates a todo
        * [X] Content is trimmed.
        * [X] Empty content does not create a new todo.
        * [X] Input is cleared when new todo is created.
* Mark all as completed button sets all todos to its state.
    * Is cleared to "off" when "Clear Completed" is used.
    * Becomes checked if all todos are checked.
    * Becomes unchecked if not all todos are checked.
* Double clicking a todo's label begins editing that todo.
* Clear completed button shows the number of todos
* Clear completed button hides when there are no todoso
