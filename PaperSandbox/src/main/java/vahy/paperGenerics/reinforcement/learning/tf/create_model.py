import tensorflow as tf

# input_count = 1

input_count = 8
 # input_count = 34

hidden_count_1 = 20
hidden_count_2 = 10
hidden_count_3 = 10

action_count = 3
# action_count = 5

x = tf.placeholder(tf.double, [None, input_count], name= 'input_node')
q_target = tf.placeholder(tf.double, [None, 1], name = 'Q_target_node')
risk_target = tf.placeholder(tf.double, [None, 1], name = 'Risk_target_node')
policy_target = tf.placeholder(tf.double, [None, action_count], name = 'Policy_target_node')

hidden_1 = tf.layers.dense(x,        hidden_count_1, tf.nn.relu, True, tf.glorot_normal_initializer(), name = "Hidden_1")
hidden_2 = tf.layers.dense(hidden_1, hidden_count_2, tf.nn.relu, True, tf.glorot_normal_initializer(), name = "Hidden_2")
# hidden_3 = tf.layers.dense(hidden_2, hidden_count_3, tf.nn.relu, True, tf.glorot_normal_initializer(), name = "Hidden_3")

policy = tf.layers.dense(hidden_2, action_count, tf.nn.softmax, kernel_initializer = tf.zeros_initializer, name = 'policy_node')
risk =   tf.layers.dense(hidden_2, 1, tf.nn.sigmoid, kernel_initializer = tf.zeros_initializer, name = "risk_node")
q =      tf.layers.dense(hidden_2, 1, kernel_initializer = tf.zeros_initializer, name = "q_node")

prediction = tf.concat([q, risk, policy], 1, name = "prediction_node_2")

# policy_loss = tf.reduce_mean(softmax_cross_entropy_with_logits_v2(logits = policy, labels = policy_target), name = 'policy_loss')

#policy_loss = tf.keras.losses.KLD(y_true = policy_target, y_pred = policy)
policy_loss = tf.keras.losses.categorical_crossentropy(y_true = policy_target, y_pred = policy)

q_loss = tf.keras.losses.mean_squared_error(y_true = q_target, y_pred = q)
r_loss = tf.keras.losses.mean_squared_error(y_true = risk_target, y_pred = risk)

total_loss = policy_loss + q_loss + r_loss
# total_loss = q_loss + r_loss
# total_loss = r_loss
# total_loss = q_loss
# total_loss = policy_loss


optimizer = tf.train.AdamOptimizer(learning_rate = 0.001, name = "Adam")
train_op = optimizer.minimize(total_loss, name = 'train_node')

init = tf.global_variables_initializer()


sess = tf.Session()
sess.run(init)


print(q.name)
#
# n_samples = 0
# avg_cost = 0.
# for epoch in range(100000):
#     n_samples = n_samples + 1
#
#     # batch_xs = [[1], [2], [3], [4]]
#
#     batch_xs = [[1, 0, 0, 0], [0, 1, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]]
#
#     batch_Q = [[96], [97], [98], [99]]
#     batch_R = [[0.9], [0.6], [0.4], [0.1]]
#     batch_Policy = [[0.02, 0.49, 0.49], [0.01, 0.01, 0.98], [0.01, 0.98, 0.01], [0.98, 0.01, 0.01]]
#     policy_cost, q_cost, r_cost, total_cost, _ = sess.run((policy_loss, q_loss, r_loss, total_loss, train_op), feed_dict= {x: batch_xs, q_target: batch_Q, risk_target: batch_R, policy_target: batch_Policy})
#
#     predictedVector = sess.run(prediction, feed_dict= {x: batch_xs, q_target: batch_Q, risk_target: batch_R, policy_target: batch_Policy})
#
#     np.set_printoptions(suppress=True)
#     print(predictedVector)
#
#     print("Epoch:", '%04d' % (epoch + 1), "total_cost = ", "{:.9f} ".format(sum(total_cost)), "q_cost = ", "{:.9f} ".format(sum(q_cost)), "r_cost = ", "{:.9f} ".format(sum(r_cost)), "policy_cost = ", "{:.9f} ".format(sum(policy_cost)))
#



# saver_def = tf.trainPolicy.Saver().as_saver_def()
#
# print('Operation to initialize variables:       ', init.name)
# print('Tensor to feed as input data:            ', x.name)
# print('Tensor to feed as training targets:      ', y_.name)


# print('Tensor to fetch as prediction:           ', y.name)
# print('Operation to trainPolicy one step:             ', train_op.name)
# print('Tensor to be fed for checkpoint filename:', saver_def.filename_tensor_name)
# print('Operation to save a checkpoint:          ', saver_def.save_tensor_name)
# print('Operation to restore a checkpoint:       ', saver_def.restore_op_name)
# print('Tensor to read value of W                ', W.value().name)
# print('Tensor to read value of b                ', b.value().name)

with open('../../../../../../resources/tfModel/graph.pb', 'wb') as f:
    f.write(tf.get_default_graph().as_graph_def().SerializeToString())
