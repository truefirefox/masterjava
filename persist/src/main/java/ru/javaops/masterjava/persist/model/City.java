package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

/**
 * Created by val on 2017-04-05.
 */
@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class City extends BaseEntity{

    private @NonNull String value;
    @Column("value_id")
    private @NonNull String valueId;

    public City(Integer id, String value, String valueId) {
        this(value, valueId);
        this.id = id;
    }
}
