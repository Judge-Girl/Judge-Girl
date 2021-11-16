package tw.waterball.judgegirl.primitives.exam;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Student;

import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Builder
public class Group {

    private Integer id;

    @Size(min = 1, max = 100)
    private String name;

    @Size(max = 10000)
    private Set<MemberId> memberIds;

    public Group(String name) {
        this(null, name, new HashSet<>());
    }

    public Group(int id, String name) {
        this(id, name, new HashSet<>());
    }

    public Group(String name, Set<MemberId> memberIds) {
        this(null, name, memberIds);
    }

    public Group(@Nullable Integer id, String name, Set<MemberId> memberIds) {
        this.id = id;
        this.name = name;
        this.memberIds = memberIds;
        validate(this);
    }

    public void addMember(MemberId memberId) {
        memberIds.add(memberId);
    }

    public void addMembers(Collection<MemberId> memberIds) {
        this.memberIds.addAll(memberIds);
    }

    public void addStudentsAsMembers(List<Student> students) {
        addMembers(mapToList(students, s -> new MemberId(s.getId())));
    }

    public void addStudentsAsMembers(Student... students) {
        addStudentsAsMembers(asList(students));
    }

    public void deleteMember(MemberId memberId) {
        this.memberIds.remove(memberId);
    }

    public void deleteMembers(Set<MemberId> memberIds) throws NotFoundException {
        this.memberIds.removeAll(memberIds);
    }
}
