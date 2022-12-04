package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Film implements Comparable<Film>{
    @EqualsAndHashCode.Include
    private int id;
    @NotBlank
    private String name;
    private String description;
    private LocalDate releaseDate;
    @Positive
    private int duration;
    private Set<Long> likes = new HashSet<>();

    @Override
    public int compareTo(Film o) {
        return Integer.compare(o.likes.size(), this.likes.size());
    }
}
