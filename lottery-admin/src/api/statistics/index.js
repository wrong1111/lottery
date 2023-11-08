import request from "@/utils/request";

export let reqStatisticsData = function () {
  return request({
    url: "/admin/statistics/get",
    method: "get",
  });
};
