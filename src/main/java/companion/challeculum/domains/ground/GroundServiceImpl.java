package companion.challeculum.domains.ground;

import companion.challeculum.domains.ground.dtos.Ground;
import companion.challeculum.domains.ground.dtos.GroundCreateDto;
import companion.challeculum.domains.ground.dtos.GroundJoined;
import companion.challeculum.domains.mission.MissionDao;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GroundServiceImpl implements GroundService {
    private final GroundDao groundDao;

    private final MissionDao missionDao;

    @Override
    public void deleteGround(long groundId) {
        Ground ground = groundDao.showGroundDetail(groundId);
        if (ground == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 그라운드를 찾지 못했습니다.");
        }
        if (ground.getStatus().equals("cancelled")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 취소된 그라운드입니다.");
        }
        if (List.of("ongoing", "completed").contains(ground.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 시작한 그라운드 입니다.");
        }

        groundDao.refundDeposit(groundId);
        groundDao.markNotAttending(groundId);
        groundDao.deleteGround(groundId);
    }

    @Override
    public Ground showGroundDetail(long groundId) {
        return groundDao.showGroundDetail(groundId);
    }

    @Override
    public List<GroundJoined> getGrounds(Integer page, Integer categoryId, Integer level) {
        // page1: 0-6
        // page2: 7-13
        // page3: 14-20
        // page4 : 21-27
        // page k :  7*( k - 1) ~ 7k-1
        final int ROWS_PER_PAGE = 7;
        Integer startRow = (page == null) ? null : 7 * (page - 1);

        return groundDao.getGrounds(startRow, ROWS_PER_PAGE, categoryId, level);
    }

    @Override
    public void createGround(GroundCreateDto groundCreateDTO) {
        groundDao.createGround(groundCreateDTO);
        missionDao.addMissionsToGround(groundCreateDTO.getMissionList());
    }

    @Override
    public List<Map<String, Object>> getMyGroundList(long userId, int page, String status) {
        final int ROWS_PER_PAGE = 7;
        int startRow = 7 * (page - 1);
        return groundDao.getMyGrounds(userId, startRow, ROWS_PER_PAGE, status);
    }
}
