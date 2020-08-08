import os

import sys
import tensorflow as tf

tf.compat.v1.disable_v2_behavior()

model_name = sys.argv[1]
input_count = int(sys.argv[2])
value_output_count = int(sys.argv[3])
action_output_count = int(sys.argv[4])
path_to_store = sys.argv[5]
seed = int(sys.argv[6])

print("INITIALIZING TF MODEL WITH SEED " + str(seed))

layer_size = 512

tf.compat.v1.reset_default_graph()
tf.compat.v1.random.set_random_seed(seed)

x = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, input_count], name= 'input_node')
target = tf.compat.v1.placeholder(tf.compat.v1.float64, [None, value_output_count], name = "target_node")
keep_prob = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "keep_prob_node")
learning_rate = tf.compat.v1.placeholder(tf.compat.v1.float64, [], name = "learning_rate_node")

hidden_1 = tf.compat.v1.layers.dense(x,        layer_size, tf.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer, name = 'hidden_1')
hidden_2 = tf.compat.v1.layers.dense(hidden_1, layer_size, tf.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer, name = 'hidden_2')
hidden_3 = tf.compat.v1.layers.dense(hidden_2, layer_size, tf.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer, name = 'hidden_3')
hidden_4 = tf.compat.v1.layers.dense(hidden_3, layer_size, tf.nn.relu, use_bias = True, kernel_initializer = tf.compat.v1.glorot_normal_initializer, name = 'hidden_4')

output = tf.compat.v1.layers.dense(hidden_4, value_output_count, tf.nn.tanh, use_bias = True, kernel_initializer = tf.compat.v1.zeros_initializer, bias_initializer = tf.compat.v1.zeros_initializer, name = 'output_node')
prediction = tf.compat.v1.identity(output, name = "prediction_node")

loss = tf.compat.v1.keras.losses.mean_squared_error(y_true = target, y_pred = output)
train_op = tf.compat.v1.train.AdamOptimizer(learning_rate = 0.01, name = "Optimizer").minimize(loss, name ='optimize_node')

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
