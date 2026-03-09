package com.simondmc;

public class Main {
    public static void main(String[] args) {
        System.out.println(new CharacterCell.Builder().content('!').foregroundColor(TerminalColor.CYAN).build().toString());
    }
}