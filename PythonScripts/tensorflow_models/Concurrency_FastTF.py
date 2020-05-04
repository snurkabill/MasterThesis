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


benchmark_name = 'FastTF'
input_count = 20
hidden_count_1 = 512
hidden_count_2 = 256
hidden_count_3 = 128
A_output_count = 3
B_output_count = 2
output_count = A_output_count + B_output_count

tf.reset_default_graph()


Relu = tf.nn.relu
Tanh = tf.nn.tanh
BatchNormalization = tf.layers.batch_normalization
Dropout = tf.layers.dropout
Dense = tf.layers.dense


x = tf.placeholder(tf.float64, [None, input_count], name= 'input_node')
x_single = tf.placeholder(tf.float64, [None, input_count], name= 'input_node')
target = tf.placeholder(tf.float64, [None, output_count], name = "target_node")
keep_prob = tf.placeholder(tf.float64, [], name = "keep_prob_node")
learning_rate = tf.placeholder(tf.float64, [], name = "learning_rate_node")

# A_target = tf.placeholder(tf.double, [None, A_output_count], name = 'A_target_node')
# B_target = tf.placeholder(tf.double, [None, B_output_count], name = 'B_target_node')

A_target = tf.slice(target, [0, 0], [-1, A_output_count], name = "A_slice_node")
B_target = tf.slice(target, [0, A_output_count], [-1, B_output_count], name = "B_slice_node")

hidden_1 = Dropout(Dense(x,        hidden_count_1, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "Hidden_1")) #, kernel_regularizer= tf.contrib.layers.l2_regularizer(scale=0.0))
hidden_2 = Dropout(Dense(hidden_1, hidden_count_2, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "Hidden_2")) #, kernel_regularizer= tf.contrib.layers.l2_regularizer(scale=0.0))
hidden_3 = Dense(hidden_2, hidden_count_3, tf.nn.relu, True, tf.glorot_normal_initializer(), name = "Hidden_3") #, kernel_regularizer= tf.contrib.layers.l2_regularizer(scale=0.0))

A_output = tf.layers.dense(hidden_3, A_output_count, tf.nn.tanh, use_bias = True, kernel_initializer = tf.zeros_initializer, bias_initializer = tf.zeros_initializer, name = 'A_output_node')
B_output = tf.layers.dense(hidden_3, B_output_count, tf.nn.sigmoid, use_bias = True, kernel_initializer = tf.zeros_initializer, name = "B_output_node")

prediction = tf.concat([A_output, B_output], 1, name = "prediction_node")

# policy_loss = tf.reduce_mean(softmax_cross_entropy_with_logits_v2(logits = policy, labels = policy_target), name = 'policy_loss')

A_loss = tf.keras.losses.mean_squared_error(y_true = A_target, y_pred = A_output)
# A_loss = tf.keras.losses.categorical_crossentropy(y_true = A_target, y_pred = A_output)
# A_loss = tf.reduce_mean(softmax_cross_entropy_with_logits_v2(logits = A_target, labels = A_output), name = 'A_loss')

# B_loss = tf.losses.mean_squared_error(labels = B_target, predictions = B_output)
# B_loss = tf.metrics.mean_squared_error(labels = B_target, predictions = B_output)
B_loss = tf.keras.losses.mean_squared_error(y_true = B_target, y_pred = B_output)

total_loss = A_loss + B_loss

train_op = tf.train.AdamOptimizer(learning_rate = 0.01, name = "Adam").minimize(total_loss, name = 'optimize_node')

init = tf.global_variables_initializer()


sess = tf.Session()


sess.run(init)

print(A_output.name)

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

