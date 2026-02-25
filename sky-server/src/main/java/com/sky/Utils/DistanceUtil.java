package com.sky.utils; // 建议包名全小写

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import com.sky.properties.BaiDuAddressProperties;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

@Component
public class DistanceUtil {

    // 地球平均半径，单位：千米(km)
    private static final double EARTH_RADIUS = 6371.0;

    @Autowired
    private BaiDuAddressProperties baiDuAddressProperties;

    /**
     * 计算两点之间的真实距离
     */
    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        // 经纬度转换成弧度
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);

        // 哈弗辛公式核心计算
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));

        s = s * EARTH_RADIUS;
        // 保留两位小数
        s = Math.round(s * 100d) / 100d;

        return s;
    }

    /**
     * 辅助方法：发送请求并解析经纬度
     */
    public double[] getCoordinatesByUrl(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                // 获取并解析 JSON 字符串
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject jsonObject = JSON.parseObject(result);

                if ("0".equals(jsonObject.getString("status"))) {
                    JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
                    double lat = location.getDouble("lat");
                    double lng = location.getDouble("lng");
                    return new double[]{lat, lng}; // 返回 [纬度, 经度]
                } else {
                    throw new OrderBusinessException("地址解析失败，请检查地址是否正确！百度错误码：" + jsonObject.getString("status"));
                }
            }
        }
    }

    /**
     * 核心业务逻辑：校验用户的收货地址是否超出配送范围（5km）
     * （注意：这里去掉了 static）
     * * @param userAddress 用户完整的收货地址
     */
    public boolean checkDistance(String userAddress) throws Exception {
        String businessAddress = baiDuAddressProperties.getAddress();

        // URL 编码
        String encodedUserAddress = URLEncoder.encode(userAddress, "UTF-8");
        String encodedBusinessAddress = URLEncoder.encode(businessAddress, "UTF-8");

        String AK = baiDuAddressProperties.getAk();
        String userURL = "https://api.map.baidu.com/geocoding/v3/?address=" + encodedUserAddress + "&output=json&ak=" + AK;
        String businessURL = "https://api.map.baidu.com/geocoding/v3/?address=" + encodedBusinessAddress + "&output=json&ak=" + AK;

        // 调用 helper 方法，分别获取两地的经纬度 [lat, lng]
        // 这里直接用 this. 就可以调用了
        double[] userCoords = this.getCoordinatesByUrl(userURL);
        double[] businessCoords = this.getCoordinatesByUrl(businessURL);

        System.out.println("用户经纬度: lat=" + userCoords[0] + ", lng=" + userCoords[1]);
        System.out.println("商家经纬度: lat=" + businessCoords[0] + ", lng=" + businessCoords[1]);

        // 计算距离
        double distance = this.getDistance(userCoords[0], userCoords[1], businessCoords[0], businessCoords[1]);
        System.out.println("两地实际距离为: " + distance + " km");

        // 判断是否超出 5km 配送范围
        if (distance > 5.0) {
            // 超出配送范围，抛出业务异常，外层会被全局异常处理器捕获并返回给前端
            throw new OrderBusinessException("超出配送范围，当前距离为：" + distance + "公里");
        }

        return true;
    }
}