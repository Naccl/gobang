package top.naccl.gobang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.naccl.gobang.model.entity.Score;
import top.naccl.gobang.model.entity.User;

@Mapper
@Repository
public interface ScoreMapper  {
    Score findScoreByUsername(String username);
}
