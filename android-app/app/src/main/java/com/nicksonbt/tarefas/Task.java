package com.nicksonbt.tarefas;

final class Task {
    final long id;
    final String text;
    boolean done;

    Task(long id, String text, boolean done) {
        this.id = id;
        this.text = text;
        this.done = done;
    }
}
