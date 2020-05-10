package org.magnum.dataup.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Repository;

@Repository
public class VideoRepositoryImpl implements VideoRepository {

    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long, Video> videos = new HashMap<Long, Video>();

    public Video save(Video entity) {

        checkAndSetId(entity);

        videos.put(entity.getId(), entity);

        return entity;
    }

    public Collection<Video> getAllVideos() {

        return videos.values();
    }

    public Optional<Video> getVideo(long id) {

        Video storedVideo = videos.get(id);
        
        return Optional.ofNullable(storedVideo);
    }

    private void checkAndSetId(Video entity) {
        if (entity.getId() == 0) {
            entity.setId(currentId.incrementAndGet());
        }
    }
}