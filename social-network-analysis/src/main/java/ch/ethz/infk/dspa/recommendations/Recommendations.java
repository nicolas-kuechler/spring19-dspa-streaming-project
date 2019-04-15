package ch.ethz.infk.dspa.recommendations;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import ch.ethz.infk.dspa.avro.Comment;
import ch.ethz.infk.dspa.avro.Like;
import ch.ethz.infk.dspa.avro.Post;
import ch.ethz.infk.dspa.recommendations.dto.PersonActivity;
import ch.ethz.infk.dspa.recommendations.ops.CategoryEnrichmentProcessFunction;
import ch.ethz.infk.dspa.recommendations.ops.CommentToPersonActivityMapFunction;
import ch.ethz.infk.dspa.recommendations.ops.FriendsFilterFunction;
import ch.ethz.infk.dspa.recommendations.ops.LikeToPersonActivityMapFunction;
import ch.ethz.infk.dspa.recommendations.ops.PersonActivityBroadcastJoinProcessFunction;
import ch.ethz.infk.dspa.recommendations.ops.PersonActivityReduceFunction;
import ch.ethz.infk.dspa.recommendations.ops.PersonOutputSelectorProcessFunction;
import ch.ethz.infk.dspa.recommendations.ops.PostToPersonActivityMapFunction;
import ch.ethz.infk.dspa.recommendations.ops.TopKAggregateFunction;
import ch.ethz.infk.dspa.stream.CommentDataStreamBuilder;
import ch.ethz.infk.dspa.stream.LikeDataStreamBuilder;
import ch.ethz.infk.dspa.stream.PostDataStreamBuilder;

public class Recommendations {

	public void start() {

		// build stream execution environment
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

		long maxDelay = 600;

		// build data streams
		DataStream<Post> postStream = new PostDataStreamBuilder(env)
				.withMaxOutOfOrderness(Time.seconds(maxDelay))
				.build();

		DataStream<Comment> commentStream = new CommentDataStreamBuilder(env)
				.withPostIdEnriched()
				.withMaxOutOfOrderness(Time.seconds(maxDelay))
				.build();

		DataStream<Like> likeStream = new LikeDataStreamBuilder(env)
				.withMaxOutOfOrderness(Time.seconds(maxDelay))
				.build();

		DataStream<PersonActivity> postPersonActivityStream = postStream.map(new PostToPersonActivityMapFunction());

		DataStream<PersonActivity> commentPersonActivityStream = commentStream
				.map(new CommentToPersonActivityMapFunction());

		DataStream<PersonActivity> likePersonActivityStream = likeStream.map(new LikeToPersonActivityMapFunction());

		SingleOutputStreamOperator<PersonActivity> personActivityStream = postPersonActivityStream
				.union(commentPersonActivityStream, likePersonActivityStream)
				.keyBy(activity -> activity.postId())
				.process(new CategoryEnrichmentProcessFunction())
				.keyBy(activity -> activity.personId())
				.window(SlidingEventTimeWindows.of(Time.hours(4), Time.hours(1)))
				.reduce(new PersonActivityReduceFunction())
				.keyBy(activity -> 0l) // TODO [rsc] should be done differently if possible, don't want to send all to
										// same operator for output selection
				.process(new PersonOutputSelectorProcessFunction());

		BroadcastStream<PersonActivity> selectedPersonActivityStream = personActivityStream
				.getSideOutput(PersonOutputSelectorProcessFunction.SELECTED)
				.broadcast(PersonActivityBroadcastJoinProcessFunction.SELECTED_PERSON_STATE_DESCRIPTOR);

		personActivityStream
				.connect(selectedPersonActivityStream)
				.process(new PersonActivityBroadcastJoinProcessFunction(10, Time.hours(1)))
				.filter(new FriendsFilterFunction())
				.keyBy(similarity -> similarity.person1Id())
				.window(TumblingEventTimeWindows.of(Time.hours(1)))
				.aggregate(new TopKAggregateFunction(10))
				.print();

	}

}