package tw.waterball.judgegirl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkProgress {

    public Homework homework;

    public Map<Integer, Verdict> progress = new TreeMap<>();

}
