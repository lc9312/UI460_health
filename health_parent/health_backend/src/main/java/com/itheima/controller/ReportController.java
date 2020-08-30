package com.itheima.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.constant.MessageConstant;
import com.itheima.entity.Result;
import com.itheima.service.MemberService;
import com.itheima.service.OrderService;
import com.itheima.service.ReportService;
import com.itheima.service.SetmealService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.tools.attach.HotSpotVirtualMachine;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 统计报表
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Reference
    private MemberService memberService;

    /**
     * 会员数量统计
     * @return
     */
    @RequestMapping("/getMemberReport")
    public Result getMemberReport(){

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,-12);//获得当前日期之前12个月的日期

        List<String> list = new ArrayList<>();
        for(int i=0;i<12;i++){
            calendar.add(Calendar.MONTH,1);
            list.add(new SimpleDateFormat("yyyy.MM").format(calendar.getTime()));
        }
        Map<String,Object> map = new HashMap<>();
        map.put("months", list);

        List<Integer> memberCount = memberService.findMemberCountByMonth(list);
        map.put("memberCount",memberCount);
        return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS,map);
    }

    @Reference
    private SetmealService setmealService;

    /**
     * 套餐占比统计
     * @return
     */
    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport(){

        List<Map<String, Object>> list = setmealService.findSetmealCount();

        Map<String,Object> map = new HashMap<>();
        map.put("setmealCount", list);

        List<String> setmealNames = new ArrayList<>();
        for(Map<String,Object> m : list){
            String name = (String) m.get("name");
            setmealNames.add(name);
        }
        map.put("setmealNames",setmealNames);

        return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS,map);
    }

    @Reference
    private ReportService reportService;

    /**
     * 获取运营统计数据
     * @return
     */
    @RequestMapping("/getBusinessReportData")
    public Result getBusinessReportData(){
        try {
            Map<String, Object> result = reportService.getBusinessReport();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS, result);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    /**
     * 导出Excel报表
     * @return
     */
    @RequestMapping("/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try{
            //远程调用报表服务获取报表数据
            Map<String, Object> result = reportService.getBusinessReport();

            //取出返回结果数据，准备将报表数据写入到Excel文件中
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");

            //获得Excel模板文件绝对路径
            String temlateRealPath = request.getSession().getServletContext().getRealPath("template") +
                            File.separator + "report_template.xlsx";
            //读取模板文件创建Excel表格对象
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(temlateRealPath)));
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);//日期

            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);//新增会员数（本日）
            row.getCell(7).setCellValue(totalMember);//总会员数

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);//本周新增会员数
            row.getCell(7).setCellValue(thisMonthNewMember);//本月新增会员数

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);//今日预约数
            row.getCell(7).setCellValue(todayVisitsNumber);//今日到诊数

            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);//本周预约数
            row.getCell(7).setCellValue(thisWeekVisitsNumber);//本周到诊数

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);//本月预约数
            row.getCell(7).setCellValue(thisMonthVisitsNumber);//本月到诊数

            int rowNum = 12;
            for(Map map : hotSetmeal){//热门套餐
                String name = (String) map.get("name");
                Long setmeal_count = (Long) map.get("setmeal_count");
                BigDecimal proportion = (BigDecimal) map.get("proportion");
                row = sheet.getRow(rowNum ++);
                row.getCell(4).setCellValue(name);//套餐名称
                row.getCell(5).setCellValue(setmeal_count);//预约数量
                row.getCell(6).setCellValue(proportion.doubleValue());//占比
            }

            //通过输出流进行文件下载
            ServletOutputStream out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("content-Disposition","attachment;filename=report.xlsx");
            workbook.write(out);

            out.flush();
            out.close();
            workbook.close();
            return null;
        }catch (Exception e){
            return new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL, null);
        }

        /**
         * 每日预约数、和到诊数统计
         * getAppointmentsAndVisitsDya
         */
    }

    /**
     * 获取每天预约、到诊数量
     * @param date
     * @return
     */
    @Reference
    private OrderService orderService;
    @RequestMapping("/getAppointmentsAndVisitDay")
    public Result getAppointmentsAndVisitDay(@RequestBody Map<String,Object> date) throws ParseException {
       /* Map<String,Map<String,Object>> result = new HashMap<>();

        Map<String,Object> appointments= new HashMap<>();
        List<Integer> appointmentsCount = new ArrayList<>();
        appointmentsCount.add(100);
        appointmentsCount.add(110);
        appointmentsCount.add(120);
        appointmentsCount.add(120);
        appointments.put("appointmentsCount",appointmentsCount);
        appointments.put("name","到诊数量");

        Map<String,Object> visits= new HashMap<>();
        List<Integer> visitsCount = new ArrayList<>();
        visitsCount.add(50);
        visitsCount.add(60);
        visitsCount.add(70);
        visitsCount.add(80);
        visits.put("visitsCount",visitsCount);
        visits.put("name","预约数量");

        Map<String,Object> months= new HashMap<>();
        //获取请求日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start = sdf.parse((String) date.get("start"));
        Date end = sdf.parse((String) date.get("end"));
        long day = (end.getTime()-start.getTime())/(24*60*60*1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        //遍历开始至结束日期封装为list
        List<String> month = new ArrayList<>();
        month.add(sdf.format(calendar.getTime()));
        for (int i = 0; i < day; i++) {
            calendar.add(Calendar.DAY_OF_MONTH,1);
            month.add(sdf.format(calendar.getTime()));
        }
        months.put("month",month);
        result.put("months",months);

        List<Map> list = orderService.findEveryDayCountBydate(date);

        result.put("appointments",appointments);
        result.put("visits",visits);
*/


        Map<String,Map<String,Object>> result = new HashMap<>();
        List<Map> list = orderService.findEveryDayCountBydate(date);
        Map<String,Object> appointments= new HashMap<>();
        List<Object> appointmentsCount = new ArrayList<>();
        Map<String,Object> visits= new HashMap<>();
        List<Object> visitsCount = new ArrayList<>();
        Map<String,Object> months= new HashMap<>();
        List<String> month = new ArrayList<>();

        for (Map map : list) {
            appointmentsCount.add((Object) map.get("appointmentsCount"));
            visitsCount.add((Object) map.get("visitsCount"));
            month.add( map.get("month")+"");
        }

        appointments.put("appointmentsCount",appointmentsCount);
        appointments.put("name","到诊数量");
        visits.put("visitsCount",visitsCount);
        visits.put("name","预约数量");
        months.put("month",month);

        result.put("months",months);
        result.put("appointments",appointments);
        result.put("visits",visits);
        return new Result(true,"获取每日数据成功",result);
    }
}