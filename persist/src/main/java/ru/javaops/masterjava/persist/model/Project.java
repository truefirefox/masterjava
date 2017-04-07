package ru.javaops.masterjava.persist.model;

import lombok.*;

import java.util.List;

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
    private List<Group> groups;

    public Project(Integer id, String description, String name) {
        this(description, name);
        this.id = id;
    }
}
