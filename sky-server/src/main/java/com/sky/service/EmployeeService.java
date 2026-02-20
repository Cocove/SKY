package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.entity.PageBean;
import com.sky.vo.EmployeeLoginVO;

import java.util.List;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    PageBean<Employee> list(String name, int page, int pageSize);

    void addEmployee(EmployeeDTO employeeDTO);
}
