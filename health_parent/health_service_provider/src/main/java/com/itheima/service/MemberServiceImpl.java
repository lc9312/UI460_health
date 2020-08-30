package com.itheima.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.dao.MemberDao;
import com.itheima.pojo.Member;
import com.itheima.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service(interfaceClass = MemberService.class)
@Transactional
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberDao memberDao;

    //根据手机号查询会员
    public Member findByTelephone(String telephone) {
        return memberDao.findByTelephone(telephone);
    }

    //新增会员
    public void add(Member member) {
        if(member.getPassword() != null){
            member.setPassword(MD5Utils.md5(member.getPassword()));
        }
        memberDao.add(member);
    }

    //根据月份统计会员数量
    public List<Integer> findMemberCountByMonth(List<String> month) {
        List<Integer> list = new ArrayList<>();
        for(String m : month){
            m = m + ".31";//格式：2019.04.31
            Integer count = memberDao.findMemberCountBeforeDate(m);
            list.add(count);
        }
        return list;
    }

    // 更新会员基本信息
    @Override
    public void updateMemberMsg(Member member) {
        memberDao.updateMemberMsg(member);
    }

    // 更新会员密码
    @Override
    public void updatePassword(String md5Pass, String phoneNumber) {
        memberDao.updatePwdByPhone(phoneNumber,md5Pass);
    }

    // 变更会员手机
    @Override
    public void updateTel(String oldPhone, String newPhone) {
        memberDao.updateTel(oldPhone,newPhone);
    }

}
