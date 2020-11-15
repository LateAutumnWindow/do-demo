package com.yan.demo.ElasticSearch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person implements Serializable {

    @JsonIgnore
    private Integer id;
    private String name;
    private Integer age;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date birthday;

}
