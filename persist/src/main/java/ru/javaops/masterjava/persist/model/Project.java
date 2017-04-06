package ru.javaops.masterjava.persist.model;

import lombok.*;

/**
 * Created by val on 2017-04-05.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Project extends BaseEntity{

    private @NonNull String description;
    private @NonNull String name;

    public Project(Integer id, String description, String name) {
        this(description, name);
        this.id = id;
    }
}
