import tensorflow as tf
import numpy as np
from tensorflow.python.ops.nn_ops import softmax_cross_entropy_with_logits_v2


def variable_summaries(var):
    """Attach a lot of summaries to a Tensor (for TensorBoard visualization)."""
    with tf.name_scope('summaries'):
        mean = tf.reduce_mean(var)
        tf.summary.scalar('mean', mean)
        with tf.name_scope('stddev'):
            stddev = tf.sqrt(tf.reduce_mean(tf.square(var - mean)))
        tf.summary.scalar('stddev', stddev)
        tf.summary.scalar('max', tf.reduce_max(var))
        tf.summary.scalar('min', tf.reduce_min(var))
        tf.summary.histogram('histogram', var)


benchmark_name = 'FastTF_probs'
input_count = 20
hidden_count_1 = 512
hidden_count_2 = 256
hidden_count_3 = 128

output_count = 5

tf.reset_default_graph()

Relu = tf.nn.relu

Tanh = tf.nn.tanh
BatchNormalization = tf.layers.batch_normalization
Dense = tf.layers.dense
Dropout = tf.nn.dropout


x = tf.placeholder(tf.float64, [None, input_count], name= 'input_node')
target = tf.placeholder(tf.float64, [None, output_count], name = "target_node")
keep_prob = tf.placeholder(tf.float64, [], name = "keep_prob_node")
learning_rate = tf.placeholder(tf.float64, [], name = "learning_rate_node")

action_target = target

hidden_1 = Dense(x,        hidden_count_1, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "hidden_1")
hidden_2 = Dense(Dropout(hidden_1, keep_prob=keep_prob), hidden_count_2, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "hidden_2")
hidden_3 = Dense(Dropout(hidden_2, keep_prob=keep_prob), hidden_count_3, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "hidden_3")

action_output = tf.layers.dense(hidden_3, output_count, tf.nn.softmax, use_bias = True, kernel_initializer = tf.zeros_initializer, bias_initializer = tf.zeros_initializer, name = "action_output_node")

prediction = tf.concat(action_output, 1, name = "concat_node")
prediction_identity = tf.identity(prediction, name = "prediction_node")

total_loss = tf.keras.losses.categorical_crossentropy(y_true = action_target, y_pred = action_output)
train_op = tf.train.AdamOptimizer(learning_rate = learning_rate, name = "Optimizer").minimize(total_loss, name = 'optimize_node')

init = tf.global_variables_initializer()

sess = tf.Session()
sess.run(init)



# n_samples = 0
# avg_cost = 0.
# for epoch in range(1000):
#     n_samples = n_samples + 1
#
#     batch_inputs  = [[1.0, 0.0, 0.0, 0.0, 0.0], [0.0, 1.0, 0.0, 0.0, 0.0], [0.0, 0.0, 1.0, 0.0, 0.0], [0.0, 0.0, 0.0, 1.0, 0.0], [0.0, 0.0, 0.0, 0.0, 1.0]]
#     batch_outputs = [[1.0, 0.0, 0.0, 1.0, 0.0], [0.0, 1.0, 0.0, 0.0, 1.0], [0.0, 0.0, 1.0, 0.5, 0.5], [1.0, 0.0, 0.0, 0.0, 1.0], [0.0, 1.0, 0.0, 1.0, 0.0]]
#
#     A_cost, B_cost, total_cost, _ = sess.run((A_loss, B_loss, total_loss, train_op), feed_dict= {x: batch_inputs, target: batch_outputs})
#
#     predictedVector = sess.run(prediction, feed_dict= {x: batch_inputs, target: batch_outputs})
#
#     np.set_printoptions(suppress=True)
#     print(predictedVector)
#
#     print("Epoch:", '%04d' % (epoch + 1), "total_cost = ", "{:.9f} ".format(sum(total_cost)), "A_cost = ", "{:.9f} ".format(sum(A_cost)), "B_cost = ", "{:.9f} ".format(sum(B_cost)))

#
#
#
# saver_def = tf.trainPolicy.Saver().as_saver_def()
#
# print('Operation to initialize variables:       ', init.name)
# print('Tensor to feed as input data:            ', x.name)
# print('Tensor to feed as training targets:      ', y_.name)
#
#
# print('Tensor to fetch as prediction:           ', y.name)
# print('Operation to trainPolicy one step:             ', train_op.name)
# print('Tensor to be fed for checkpoint filename:', saver_def.filename_tensor_name)
# print('Operation to save a checkpoint:          ', saver_def.save_tensor_name)
# print('Operation to restore a checkpoint:       ', saver_def.restore_op_name)
# print('Tensor to read value of W                ', W.value().name)
# print('Tensor to read value of b                ', b.value().name)


path = '../generated_models/graph_' + benchmark_name + '.pb'
print(path)
with open(path, 'wb') as f:
    f.write(tf.get_default_graph().as_graph_def().SerializeToString())




# builder = tf.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.saved_model.tag_constants.SERVING]
# )
# builder.save()

