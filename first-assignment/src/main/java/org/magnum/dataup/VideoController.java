/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.magnum.dataup.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoController {

    private VideoRepository videoRepository;

    private static final int NOT_FOUND = 404;

    private static final int OK = 200;

    private static final int INTERNAL_ERROR = 500;

    @Autowired
    public void setVideoRepository(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    @ResponseBody
    public Collection<Video> getVideoList() {
        return videoRepository.getAllVideos();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    @ResponseBody
    public Video addVideo(@RequestBody final Video video) {

        Video savedVideo = videoRepository.save(video);

        long videoId = savedVideo.getId();

        String videoUrl = UrlBuilder.getDataUrl(videoId);

        savedVideo.setDataUrl(videoUrl);

        return savedVideo;
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<VideoStatus> setVideoData(@RequestParam("data") final MultipartFile videoData,
            @PathVariable("id") final long id) throws IOException {

        Optional<Video> optionalVideo = videoRepository.getVideo(id);

        if (!optionalVideo.isPresent()) {
            return new ResponseEntity<VideoStatus>(HttpStatus.NOT_FOUND);
        }

        VideoFileManager videoFileManger = VideoFileManager.get();
        videoFileManger.saveVideoData(optionalVideo.get(), videoData.getInputStream());

        return new ResponseEntity<VideoStatus>(new VideoStatus(VideoState.READY), HttpStatus.OK);
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
    public HttpServletResponse getVideoData(@PathVariable("id") final long id, HttpServletResponse response)
            throws IOException {

        Optional<Video> optionalVideo = videoRepository.getVideo(id);
        if (!optionalVideo.isPresent()) {
            response.sendError(NOT_FOUND);
            response.setStatus(NOT_FOUND);
            return response;
        }

        try {
            VideoFileManager videoFileManger = VideoFileManager.get();
            Video video = optionalVideo.get();
            videoFileManger.copyVideoData(video, response.getOutputStream());
        } catch (IOException exception) {
            response.setStatus(INTERNAL_ERROR);
            return response;
        }

        response.setContentType("video/mp4");
        response.setStatus(OK);
        return response;
    }

}
