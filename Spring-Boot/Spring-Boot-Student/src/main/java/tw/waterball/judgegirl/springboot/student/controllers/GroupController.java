package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupDataRepository;

/**
 * @author - wally55077@gmail.com
 */
@RestController
@AllArgsConstructor
public class GroupController {

    private final GroupDataRepository repository;


    @PostMapping("/api/groups")
    public GroupData createGroup(@RequestBody GroupCreateRequest request) {
        String title = request.getTitle();
        if (repository.existsGroupDataByTitle(title)) {
            throw new DuplicateGroupTitleException();
        }
        GroupData groupData = GroupData.builder()
                .title(title).build();
        return repository.saveAndFlush(groupData);
    }

    @ExceptionHandler({DuplicateGroupTitleException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }

}
