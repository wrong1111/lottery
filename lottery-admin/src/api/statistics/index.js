import request from "@/utils/request";

export let reqStatisticsData = function () {
  return request({
    url: "/admin/statistics/get",
    method: "get",
  });
};

export let reqDayStatisticsData = function (data) {
  return request({
    url: "/admin/statistics/day",
    method: "POST",
    data:data
  });
};

export let reqMonthStatisticsData = function (data) {
  return request({
    url: "/admin/statistics/month",
    method: "POST",
    data:data
  });
};
