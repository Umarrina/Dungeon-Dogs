package ru.kpfu.itis.group400.amirova.server.game.model.dogs;

import ru.kpfu.itis.group400.amirova.server.game.model.DamageType;
import ru.kpfu.itis.group400.amirova.server.game.model.decks.DeckRooms;

import java.util.EnumMap;

public class Dog {
    private String name;
    private GradeType grade;
    private String ability;
    private String description;

    private EnumMap<DamageType, Integer> damage;

    private int maxHealth;


    // конструктор, который нужен для первичной инициализации собаки для игрока


    public Dog(String name, GradeType grade, String ability, String description,
               EnumMap<DamageType, Integer> damage, int maxHealth) {
        this.name = name;
        this.grade = grade;
        this.ability = ability;
        this.description = description;
        this.damage = damage;
        this.maxHealth = maxHealth;
    }

    public void addMaxHealth(int amount) {
        maxHealth += amount;
    }

    public String getName() {
        return name;
    }

    public GradeType getGrade() {
        return grade;
    }

    public String getAbility() {
        return ability;
    }

    public String getDescription() {
        return description;
    }

    public EnumMap<DamageType, Integer> getDamage() {
        return damage;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
}
