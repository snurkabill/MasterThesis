import os

import sys
import tensorflow._api.v2.compat.v1 as tf

tf.disable_v2_behavior()


model_name = sys.argv[1]
input_count = int(sys.argv[2])
value_output_count = int(sys.argv[3])
action_output_count = int(sys.argv[4])
path_to_store = sys.argv[5]
seed = int(sys.argv[6])

print("INITIALIZING tf MODEL WITH SEED" + str(seed))

hidden_count_1 = 128
hidden_count_2 = 64
hidden_count_3 = 32
hidden_count_4 = 32
hidden_count_5 = 32

Q_output_count = value_output_count
risk_output_count = value_output_count

output_count = Q_output_count + risk_output_count + action_output_count

tf.compat.v1.reset_default_graph()
tf.compat.v1.random.set_random_seed(seed)

Relu = tf.compat.v1.nn.relu

Tanh = tf.compat.v1.nn.tanh
BatchNormalization = tf.compat.v1.layers.batch_normalization
Dense = tf.compat.v1.layers.dense
Dropout = tf.compat.v1.nn.dropout


x = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, input_count], name= 'input_node')
target = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, output_count], name = "target_node")
keep_prob = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "keep_prob_node")
learning_rate = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "learning_rate_node")

Q_target = tf.compat.v1.slice(target, [0, 0], [-1, Q_output_count], name = "Q_slice_node")
risk_target = tf.compat.v1.slice(target, [0, Q_output_count], [-1, risk_output_count], name = "risk_slice_node")
action_target = tf.compat.v1.slice(target, [0, Q_output_count + risk_output_count], [-1, action_output_count], name = "action_slice_node")

hidden_1 = Dense(x,        hidden_count_1, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_1")
hidden_2 = Dense(Dropout(hidden_1, keep_prob=keep_prob), hidden_count_2, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_2")
hidden_3 = Dense(Dropout(hidden_2, keep_prob=keep_prob), hidden_count_3, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_3")
hidden_4 = Dense(Dropout(hidden_3, keep_prob=keep_prob), hidden_count_4, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_4")
hidden_5 = Dense(Dropout(hidden_4, keep_prob=keep_prob), hidden_count_5, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_5")


Q_output      = tf.compat.v1.layers.dense(hidden_3, Q_output_count,                   use_bias = True, kernel_initializer = tf.compat.v1.zeros_initializer, bias_initializer = tf.compat.v1.zeros_initializer, name = 'Q_output_node')
risk_output   = tf.compat.v1.layers.dense(hidden_3, risk_output_count, tf.compat.v1.nn.sigmoid, use_bias = True, kernel_initializer = tf.compat.v1.zeros_initializer, bias_initializer = tf.compat.v1.zeros_initializer, name = "risk_output_node")
action_output = tf.compat.v1.layers.dense(hidden_3, action_output_count, tf.compat.v1.nn.softmax, use_bias = True, kernel_initializer = tf.compat.v1.zeros_initializer, bias_initializer = tf.compat.v1.zeros_initializer, name = "action_output_node")

prediction = tf.compat.v1.concat([Q_output, risk_output, action_output], 1, name = "concat_node")
prediction_identity = tf.compat.v1.identity(prediction, name = "prediction_node")

Q_loss = tf.compat.v1.keras.losses.mean_squared_error(y_true = Q_target, y_pred = Q_output)
risk_loss = tf.compat.v1.keras.losses.mean_squared_error(y_true = risk_target, y_pred = risk_output)
policy_loss = tf.compat.v1.keras.losses.categorical_crossentropy(y_true = action_target, y_pred = action_output)

total_loss = Q_loss + risk_loss + policy_loss
train_op = tf.compat.v1.train.AdamOptimizer(learning_rate = learning_rate, name = "Optimizer").minimize(total_loss, name = 'optimize_node')

init = tf.compat.v1.global_variables_initializer()

sess = tf.compat.v1.Session()
sess.run(init)
train_writer = tf.compat.v1.summary.FileWriter(path_to_store + "/summary", sess.graph)
train_writer.close()


with open(os.path.join(path_to_store, model_name + '.pb'), 'wb') as f:
    f.write(tf.compat.v1.get_default_graph().as_graph_def().SerializeToString())



# builder = tf.compat.v1.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.compat.v1.saved_model.tag_constants.SERVING]
# )
# builder.save()

