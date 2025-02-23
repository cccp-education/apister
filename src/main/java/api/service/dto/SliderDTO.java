package api.service.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link api.domain.Slider} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SliderDTO implements Serializable {

    private Long id;

    @NotNull(message = "must not be null")
    private String presentation;

    @NotNull
    private UserDTO user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SliderDTO)) {
            return false;
        }

        SliderDTO sliderDTO = (SliderDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, sliderDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SliderDTO{" +
            "id=" + getId() +
            ", presentation='" + getPresentation() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
