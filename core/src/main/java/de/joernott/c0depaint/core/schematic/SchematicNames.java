package de.joernott.c0depaint.core.schematic;

import java.util.Locale;

/**
 * The names of schematics and folders in the folder {@code schematics} (docs/concept.md#files): relative, with
 * {@code /} between folders, never leaving the folder and without characters that hide what a name is. Checked on the
 * server before a request is sent and again on the client before it reads anything (task 9.5).
 */
public final class SchematicNames {
    /** The longest name. */
    public static final int MAX_LENGTH = 255;

    private SchematicNames() {}

    /**
     * Checks the name of a schematic file.
     *
     * @throws IllegalArgumentException with the reason if the name isn't allowed
     */
    public static String file(String name) {
        String checked = path(name, false);
        String lower = checked.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".litematic") && !lower.endsWith(".nbt")) {
            throw new IllegalArgumentException("The schematic " + checked + " is neither a .litematic nor a .nbt file");
        }
        return checked;
    }

    /**
     * Checks the name of a folder inside {@code schematics}; "" is the folder itself.
     *
     * @throws IllegalArgumentException with the reason if the name isn't allowed
     */
    public static String folder(String name) {
        return path(name, true);
    }

    private static String path(String name, boolean emptyAllowed) {
        if (name == null || name.isEmpty()) {
            if (emptyAllowed) {
                return "";
            }
            throw new IllegalArgumentException("The name of the schematic is empty");
        }
        if (name.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("The name is longer than " + MAX_LENGTH + " characters");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isISOControl(c) || hidden(c)) {
                throw new IllegalArgumentException(
                        "The name has the hidden character \\u" + String.format("%04X", (int) c));
            }
            if (c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|') {
                throw new IllegalArgumentException("The name has the character " + c + "; folders are separated by /");
            }
        }
        if (name.startsWith("/")) {
            throw new IllegalArgumentException("The name " + name + " starts with /; names are relative to schematics");
        }
        String trimmed = name.endsWith("/") && emptyAllowed ? name.substring(0, name.length() - 1) : name;
        for (String part : trimmed.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..") || !part.strip().equals(part)
                    || part.endsWith(".")) {
                throw new IllegalArgumentException("The name " + name + " has a part that isn't allowed: '" + part + "'");
            }
        }
        return trimmed;
    }

    /** Invisible characters and those that change the direction of text, which can hide a name. */
    private static boolean hidden(char c) {
        return (c >= '​' && c <= '‏')
                || (c >= '‪' && c <= '‮')
                || (c >= '⁠' && c <= '⁩')
                || c == '﻿'
                || c == '­'
                || Character.getType(c) == Character.FORMAT;
    }
}
