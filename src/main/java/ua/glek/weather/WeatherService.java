package ua.glek.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Service
public class WeatherService {

    private final RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }



    public String getWeather(String city){
        String cacheKey = "weather"+city;
        String cachedWeather = redisTemplate.opsForValue().get(cacheKey);

        if(cachedWeather != null){
            return cachedWeather;
        }
        try {
            String url = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"
                    + city + "?key=" + apiKey;
            String weather = restTemplate.getForObject(url, String.class);

            redisTemplate.opsForValue().set(cacheKey,weather,12, TimeUnit.HOURS);

            return weather;
        }catch (RestClientException e){
            throw new RuntimeException("Failed to fetch weather data",e);
        }
    }
}
