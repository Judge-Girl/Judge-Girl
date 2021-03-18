package tw.waterball.judgegirl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Exam {

    private Integer id = null;

    @NotBlank
    private String name;

    @NotNull
    private Date startTime;

    @NotNull
    private Date endTime;

    public Exam(String name, Date startTime, Date endTime) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void validate() {
        JSR380Utils.validate(this);
        if (startTime.after(endTime)) {
            throw new IllegalStateException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id) &&
                name.equals(exam.name) &&
                startTime.equals(exam.startTime) &&
                endTime.equals(exam.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, startTime, endTime);
    }
}