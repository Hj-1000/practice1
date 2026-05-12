package com.jeonhong.book.springboot.web.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat; //assertj 테스트 검증 라이브러리

public class HelloResponseDtoTest {

    @Test
    public void 롬복_기능_테스트(){
        //given
        String name = "test";
        int amount = 1000;

        //when
        HelloResponseDTO dto = new HelloResponseDTO(name, amount);

        //then
        //assertThat은 assertj의 테스트 검증 메서드
        //isEqualTo는 assertj의 동등 비교 메서드. assertThat에 있는 값과 isEqualTo의 값을 비교해서 같을때만 성공
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getAmount()).isEqualTo(amount);


    }

}
