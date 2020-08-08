import os

import sys
<<<<<<< HEAD
import tensorflow._api.v2.compat.v1 as tf

tf.disable_v2_behavior()
=======
import tensorflow as tf

tf.compat.v1.disable_v2_behavior()
>>>>>>> 02338fb... Missing files

model_name = sys.argv[1]
input_count = int(sys.argv[2])
value_output_count = int(sys.argv[3])
action_output_count = int(sys.argv[4])
path_to_store = sys.argv[5]
seed = int(sys.argv[6])

print("INITIALIZING TF MODEL WITH SEED" + str(seed))

hidden_count_1 = 10

output_count = value_output_count

<<<<<<< HEAD
tf.reset_default_graph()
tf.random.set_random_seed(seed)

Relu = tf.nn.relu

Tanh = tf.nn.tanh
BatchNormalization = tf.layers.batch_normalization
Dense = tf.layers.dense
Dropout = tf.nn.dropout

x = tf.placeholder(tf.float64, [None, input_count], name= 'input_node')
Q_target = tf.placeholder(tf.float64, [None, output_count], name = "target_node")
keep_prob = tf.placeholder(tf.float64, [], name = "keep_prob_node")
learning_rate = tf.placeholder(tf.float64, [], name = "learning_rate_node")

hidden_1 = Dense(x,        hidden_count_1, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "hidden_1")
hidden_2 = Dense(hidden_1, hidden_count_1, tf.nn.relu, use_bias = True, kernel_initializer = tf.glorot_normal_initializer(), name = "hidden_2")

output = tf.layers.dense(hidden_2, output_count, tf.nn.tanh, use_bias = True, kernel_initializer = tf.zeros_initializer, bias_initializer = tf.zeros_initializer, name = 'output_node')

prediction = tf.identity(output, name = "prediction_node")

loss = tf.keras.losses.mean_squared_error(y_true = Q_target, y_pred = output)
train_op = tf.train.AdamOptimizer(learning_rate = learning_rate, name = "Optimizer").minimize(loss, name ='optimize_node')

init = tf.global_variables_initializer()

sess = tf.Session()
sess.run(init)
train_writer = tf.summary.FileWriter(path_to_store + "/summary", sess.graph)
=======
tf.compat.v1.reset_default_graph()
tf.compat.v1.random.set_random_seed(seed)

Relu = tf.compat.v1.nn.relu

Tanh = tf.compat.v1.nn.tanh
BatchNormalization = tf.compat.v1.layers.batch_normalization
Dense = tf.compat.v1.layers.dense
Dropout = tf.compat.v1.nn.dropout

x = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, input_count], name= 'input_node')
Q_target = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, output_count], name = "target_node")
keep_prob = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "keep_prob_node")
learning_rate = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "learning_rate_node")

hidden_1 = Dense(x,        hidden_count_1, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_1")
hidden_2 = Dense(hidden_1, hidden_count_1, tf.compat.v1.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer(), name = "hidden_2")

output = tf.compat.v1.layers.dense(hidden_2, output_count, tf.nn.tanh, use_bias = True, kernel_initializer = tf.compat.v1.zeros_initializer, bias_initializer = tf.compat.v1.zeros_initializer, name = 'output_node')

prediction = tf.compat.v1.identity(output, name = "prediction_node")

loss = tf.compat.v1.keras.losses.mean_squared_error(y_true = Q_target, y_pred = output)
train_op = tf.compat.v1.train.AdamOptimizer(learning_rate = learning_rate, name = "Optimizer").minimize(loss, name ='optimize_node')

init = tf.compat.v1.global_variables_initializer()

sess = tf.compat.v1.Session()
sess.run(init)
train_writer = tf.compat.v1.summary.FileWriter(path_to_store + "/summary", sess.graph)
>>>>>>> 02338fb... Missing files
train_writer.close()


with open(os.path.join(path_to_store, model_name + '.pb'), 'wb') as f:
<<<<<<< HEAD
    f.write(tf.get_default_graph().as_graph_def().SerializeToString())



# builder = tf.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.saved_model.tag_constants.SERVING]
=======
    f.write(tf.compat.v1.get_default_graph().as_graph_def().SerializeToString())



# builder = tf.compat.v1.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.compat.v1.saved_model.tag_constants.SERVING]
>>>>>>> 02338fb... Missing files
# )
# builder.save()

