import os

import sys
import tensorflow as tf


model_name = sys.argv[1]
input_count = int(sys.argv[2])
action_output_count = int(sys.argv[3])
path_to_store = sys.argv[4]
seed = int(sys.argv[5])

print("INITIALIZING TF MODEL WITH SEED" + str(seed))

hidden_count_1 = 100
hidden_count_2 = 100
hidden_count_3 = 100

output_count = action_output_count

tf.reset_default_graph()
tf.random.set_random_seed(seed)

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

action_output = tf.layers.dense(hidden_3, action_output_count, tf.nn.softmax, use_bias = True, kernel_initializer = tf.zeros_initializer, bias_initializer = tf.zeros_initializer, name = "action_output_node")

prediction = tf.concat(action_output, 1, name = "concat_node")
prediction_identity = tf.identity(prediction, name = "prediction_node")

total_loss = tf.keras.losses.categorical_crossentropy(y_true = action_target, y_pred = action_output)
train_op = tf.train.AdamOptimizer(learning_rate = learning_rate, name = "Optimizer").minimize(total_loss, name = 'optimize_node')

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

