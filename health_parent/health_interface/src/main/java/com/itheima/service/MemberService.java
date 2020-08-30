package com.itheima.service;

import com.itheima.pojo.Member;

import java.util.List;

public interface MemberService {

    public void add(Member member);

    public Member findByTelephone(String telephone);

    public List<Integer> findMemberCountByMonth(List<String> month);

    void updateMemberMsg(Member member);

    void updatePassword(String md5Pass, String phoneNumber);

    void updateTel(String oldPhone, String newPhone);
}
