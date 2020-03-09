import sys
import numpy as np
import tensorflow as tf



model_name = sys.argv[1]
input_count = 5
hidden_count_1 = 10
# hidden_count_2 = 100
# hidden_count_3 = 10

action_output_count = int(sys.argv[3])
path_to_store = sys.argv[4]

A_output_count = 3
B_output_count = 2
output_count = A_output_count + B_output_count


Relu = tf.keras.activations.relu
Tanh = tf.keras.activations.tanh
Softmax = tf.keras.activations.softmax
BatchNormalization = tf.keras.layers.BatchNormalization
Dense = tf.keras.layers.Dense
Dropout = tf.keras.layers.Dropout
Input_ = tf.keras.Input
GlorotNormal = tf.keras.initializers.glorot_normal
Zeros = tf.keras.initializers.zeros

# from tf.keras import Model, Input
# from tensorflow.python.keras.layers import Dense, Embedding, concatenate, Flatten, Dropout, BatchNormalization



x = Input_(input_count, name= 'input_node')
target = Input_(output_count, name = "target_node")
keep_prob = Input_([], name = "keep_prob_node")
learning_rate = Input_([], name = "learning_rate_node")

A_target = tf.slice(target, [0, 0], [-1, A_output_count], name = "A_slice_node")
B_target = tf.slice(target, [0, A_output_count], [-1, B_output_count], name = "B_slice_node")

hidden_1 = Dense(units = hidden_count_1, activation = Relu, use_bias = True, kernel_initializer = GlorotNormal, name = "hidden_1")(x)

A_output = Dense(A_output_count,                   use_bias = True, kernel_initializer = Zeros, bias_initializer = Zeros, name = 'Q_output_node')(hidden_1)
B_output = Dense(B_output_count, Tanh, use_bias = True, kernel_initializer = Zeros, bias_initializer = Zeros, name = "risk_output_node")(hidden_1)

prediction = tf.concat([A_output, B_output], 1, name = "concat_node")
prediction_identity = tf.identity(prediction, name = "prediction_node")


# model = Model(
#     inputs=[x],
#     outputs=[Q_output, risk_output, action_output]
# )
#



A_loss = tf.keras.losses.mean_squared_error(y_true = A_target, y_pred = A_output)
B_loss = tf.keras.losses.binary_crossentropy(y_true = B_target, y_pred = B_output)

total_loss = A_loss + B_loss
train_op = tf.compat.v1.keras.optimizers.Adam(learning_rate = 0.001, name = "Optimizer").minimize(total_loss, name = 'optimize_node')

init = tf.global_variables_initializer()

sess = tf.Session()

sess.run(init)




n_samples = 0
avg_cost = 0.
for epoch in range(1000):
    n_samples = n_samples + 1

    batch_inputs  = [[1.0, 0.0, 0.0, 0.0, 0.0], [0.0, 1.0, 0.0, 0.0, 0.0], [0.0, 0.0, 1.0, 0.0, 0.0], [0.0, 0.0, 0.0, 1.0, 0.0], [0.0, 0.0, 0.0, 0.0, 1.0]]
    batch_outputs = [[1.0, 0.0, 0.0, 1.0, 0.0], [0.0, 1.0, 0.0, 0.0, 1.0], [0.0, 0.0, 1.0, 0.5, 0.5], [1.0, 0.0, 0.0, 0.0, 1.0], [0.0, 1.0, 0.0, 1.0, 0.0]]

    A_cost, B_cost, total_cost, _ = sess.run((A_loss, B_loss, total_loss, train_op), feed_dict= {x: batch_inputs, target: batch_outputs})
    predictedVector = sess.run(prediction, feed_dict= {x: batch_inputs, target: batch_outputs})

    np.set_printoptions(suppress=True)
    print(predictedVector)

    print("Epoch:", '%04d' % (epoch + 1), "total_cost = ", "{:.9f} ".format(sum(total_cost)), "A_cost = ", "{:.9f} ".format(sum(A_cost)), "B_cost = ", "{:.9f} ".format(sum(B_cost)))




train_writer = tf.summary.FileWriter(path_to_store + "/summary", sess.graph)
train_writer.close()


with open(path_to_store + "/" + model_name + '.pb', 'wb') as f:
    f.write(tf.get_default_graph().as_graph_def().SerializeToString())



# builder = tf.saved_model.builder.SavedModelBuilder("C:/Users/Snurka/init_model")
# builder.add_meta_graph_and_variables(
#   sess,
#   [tf.saved_model.tag_constants.SERVING]
# )
# builder.save()

