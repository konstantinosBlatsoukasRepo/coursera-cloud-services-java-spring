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

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
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

@Controller
@RequestMapping("video")
public class VideoController {

	private VideoRepository videoRepository;

	@Autowired
	public void setVideoRepository(final VideoRepository videoRepository) {
		this.videoRepository = videoRepository;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Iterable<Video> getAll() {
		return videoRepository.findAll();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Video> add(@RequestBody final Video video) {
		Video savedVideo = videoRepository.save(video);
		return new ResponseEntity<Video>(savedVideo, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Video> get(@PathVariable final long id) {

		final Video video = videoRepository.findOne(id);

		if (video == null) {
			return new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Video>(video, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}/like", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Video> like(@PathVariable final long id, Principal principal) {

		final Video video = videoRepository.findOne(id);

		if (video == null) {
			return new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
		}

		String userName = principal.getName();

		Set<String> usersLikedVideo = video.getLikedBy();
		if (usersLikedVideo.contains(userName)) {
			return new ResponseEntity<Video>(HttpStatus.BAD_REQUEST);
		}

		long increasedLikes = video.getLikes() + 1;
		video.setLikes(increasedLikes);

		usersLikedVideo.add(userName);
		video.setLikedBy(usersLikedVideo);
		Video updatedVideo = videoRepository.save(video);

		return new ResponseEntity<Video>(updatedVideo, HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}/unlike", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Video> unlike(@PathVariable final long id, Principal principal) {
		final Video video = videoRepository.findOne(id);

		if (video == null) {
			return new ResponseEntity<Video>(HttpStatus.NOT_FOUND);
		}

		String userName = principal.getName();

		Set<String> usersLikedVideo = video.getLikedBy();
		if (!usersLikedVideo.contains(userName)) {
			return new ResponseEntity<Video>(HttpStatus.BAD_REQUEST);
		}

		long increasedLikes = video.getLikes() - 1;
		video.setLikes(increasedLikes);

		usersLikedVideo.remove(userName);
		video.setLikedBy(usersLikedVideo);
		Video updatedVideo = videoRepository.save(video);

		return new ResponseEntity<Video>(updatedVideo, HttpStatus.OK);
	}

	@RequestMapping(value = "/search/findByName", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Collection<Video>> getByName(@RequestParam String title) {

		Collection<Video> videos = videoRepository.findByName(title);

		return new ResponseEntity<Collection<Video>>(videos, HttpStatus.OK);
	}

	@RequestMapping(value = "/search/findByDurationLessThan", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Collection<Video>> getByDuration(@RequestParam long duration) {

		Collection<Video> videos = videoRepository.findByDurationLessThan(duration);

		return new ResponseEntity<Collection<Video>>(videos, HttpStatus.OK);
	}

	@RequestMapping(value = "/go", method = RequestMethod.GET)
	public @ResponseBody String goodLuck() {
		return "Good Luck!";
	}

}
