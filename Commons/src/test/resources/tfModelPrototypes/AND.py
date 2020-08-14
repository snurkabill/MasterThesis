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

print("INITIALIZING TF MODEL WITH SEED " + str(seed))

tf.reset_default_graph()
tf.random.set_random_seed(seed)

x = tf.placeholder(tf.float64, [None, 2], name= 'input_node')
target = tf.placeholder(tf.float64, [None, 1], name = "target_node")
keep_prob = tf.placeholder(tf.float64, [], name = "keep_prob_node")
learning_rate = tf.placeholder(tf.float64, [], name = "learning_rate_node")

output = tf.layers.dense(x, 1, tf.nn.tanh, use_bias = True, kernel_initializer = tf.zeros_initializer, bias_initializer = tf.zeros_initializer, name = 'output_node')
prediction = tf.identity(output, name = "prediction_node")

loss = tf.keras.losses.mean_squared_error(y_true = target, y_pred = output)
train_op = tf.train.AdamOptimizer(learning_rate = 0.01, name = "Optimizer").minimize(loss, name ='optimize_node')

init = tf.global_variables_initializer()

sess = tf.Session()
sess.run(init)
train_writer = tf.summary.FileWriter(path_to_store + "/summary", sess.graph)
train_writer.close()


with open(os.path.join(path_to_store, model_name + '.pb'), 'wb') as f:
    f.write(tf.get_default_graph().as_graph_def().SerializeToString())



# builder = tf.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.saved_model.tag_constants.SERVING]
# )
# builder.save()

