package org.magnum.dataup.repository;

import java.util.Collection;
import java.util.Optional;

import org.magnum.dataup.model.Video;

public interface VideoRepository {
    
    public Video save(Video entity);

    public Collection<Video> getAllVideos();

    public Optional<Video> getVideo(long id);
}