package toonpick.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import toonpick.app.dto.AuthorDTO;
import toonpick.app.dto.GenreDTO;
import toonpick.app.dto.WebtoonDTO;
import toonpick.app.dto.WebtoonRequestDTO;
import toonpick.app.service.WebtoonService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/webtoon-request")
@RequiredArgsConstructor
public class DataRequestController {

    private final WebtoonService webtoonService;

    @PostMapping
    public ResponseEntity<List<WebtoonDTO>> createWebtoons(@RequestBody List<WebtoonRequestDTO> webtoonRequests) {
        List<WebtoonDTO> createdWebtoons = webtoonRequests.stream()
                .map(this::convertToWebtoonDTO)
                .map(webtoonService::createWebtoon)
                .collect(Collectors.toList());

        return ResponseEntity.ok(createdWebtoons);
    }



    private static final Map<String, DayOfWeek> DAY_OF_WEEK_MAP = Map.of(
            "일", DayOfWeek.SUNDAY,
            "월", DayOfWeek.MONDAY,
            "화", DayOfWeek.TUESDAY,
            "수", DayOfWeek.WEDNESDAY,
            "목", DayOfWeek.THURSDAY,
            "금", DayOfWeek.FRIDAY,
            "토", DayOfWeek.SATURDAY
    );

    private WebtoonDTO convertToWebtoonDTO(WebtoonRequestDTO request) {
        // Authors DTO 변환
        Set<AuthorDTO> authorDTOs = request.getAuthors().stream()
                .map(authorRequest -> AuthorDTO.builder()
                        .name(authorRequest.getName())
                        .role(authorRequest.getRole())
                        .link(authorRequest.getLink())
                        .build())
                .collect(Collectors.toSet());

        // Genres DTO 변환
        Set<GenreDTO> genreDTOs = request.getGenres().stream()
                .map(genreName -> GenreDTO.builder().name(genreName).build())
                .collect(Collectors.toSet());

        // DayOfWeek 변환
        DayOfWeek dayOfWeek = DAY_OF_WEEK_MAP.getOrDefault(request.getDay(), DayOfWeek.MONDAY); // 기본값 설정

        // Webtoon DTO 생성
        return WebtoonDTO.builder()
                .title(request.getTitle())
                .averageRating(Float.parseFloat(request.getRating()))
                .platformRating(Float.parseFloat(request.getRating())) // 예시로 동일하게 처리
                .description(request.getStory())
                .episodeCount(request.getEpisodeCount())
                .serializationStartDate(LocalDate.now()) // 현재 날짜 사용
                .serializationDay(dayOfWeek)
                .thumbnailUrl(request.getThumbnailUrl())
                .url(request.getUrl())
                .ageRating(request.getAgeRating())
                .authors(authorDTOs)
                .genres(genreDTOs)
                .build();
    }
}