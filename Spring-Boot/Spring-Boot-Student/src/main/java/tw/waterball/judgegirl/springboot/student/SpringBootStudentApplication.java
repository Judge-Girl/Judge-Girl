/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.student;

import org.springframework.boot.SpringApplication;
import tw.waterball.judgegirl.springboot.profiles.JudgeGirlApplication;

import java.util.Map;

@JudgeGirlApplication
public class SpringBootStudentApplication {

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        System.out.println("-------------------------");
        env.forEach((u, v) -> System.out.println(u + "=" + v));
        System.out.println("-------------------------");

        SpringApplication.run(SpringBootStudentApplication.class, args);
    }
}
