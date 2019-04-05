package ch.ethz.infk.dspa.recommendations.ops;

import java.util.List;

import org.apache.flink.api.common.functions.MapFunction;

import ch.ethz.infk.dspa.avro.Post;
import ch.ethz.infk.dspa.recommendations.dto.Category;
import ch.ethz.infk.dspa.recommendations.dto.PersonActivity;

public class PostToPersonActivityMapFunction implements MapFunction<Post, PersonActivity> {

	private static final long serialVersionUID = 1L;

	@Override
	public PersonActivity map(Post post) throws Exception {

		PersonActivity activity = new PersonActivity();

		// set postId and personId
		activity.setPostId(post.getId());
		activity.setPersonId(post.getPersonId());

		// TODO [nku] check if want to keep creationTime in PersonActivity

		// set categories of post
		List<Long> tags = post.getTags();
		if (tags != null) {
			tags.forEach(tagId -> activity.count(Category.tag(tagId)));
		}

		Long forumId = post.getForumId();
		if (forumId != null) {
			activity.count(Category.forum(forumId));
		}

		Long placeId = post.getPlaceId();
		if (placeId != null) {
			activity.count(Category.place(placeId));
		}

		String language = post.getLanguage();
		if (language != null) {
			activity.count(Category.language(language));
		}

		// TODO potentially add content topic extraction

		return activity;
	}

}
