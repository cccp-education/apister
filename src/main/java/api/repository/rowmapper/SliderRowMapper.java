package api.repository.rowmapper;

import api.domain.Slider;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Slider}, with proper type conversions.
 */
@Service
public class SliderRowMapper implements BiFunction<Row, String, Slider> {

    private final ColumnConverter converter;

    public SliderRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Slider} stored in the database.
     */
    @Override
    public Slider apply(Row row, String prefix) {
        Slider entity = new Slider();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setPresentation(converter.fromRow(row, prefix + "_presentation", String.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        return entity;
    }
}
