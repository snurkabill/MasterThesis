import os
import sys
import tensorflow as tf


model_name = sys.argv[1]
input_count = int(sys.argv[2])
value_output_count = int(sys.argv[3])
action_output_count = int(sys.argv[4])
path_to_store = sys.argv[5]
seed = int(sys.argv[6])


class Model(tf.keras.Model):
    '''Implements model architecture.'''

    def __init__(self, input_dim, output_dim, layer_size = 128,  num_layers = 1,  **kwargs):
        super().__init__(**kwargs)

        if(num_layers < 1):
            raise Exception("Can't have [" + str(num_layers) + "] number of layers")

        self.input_dim = input_dim
        self.output_dim = output_dim

        self.input_layer = tf.keras.layers.Input(shape = (None, input_dim))
        self.model_body = tf.keras.Sequential([tf.keras.layers.Dense(layer_size) for i in range(num_layers)])
        self.linear_output = tf.keras.layers.Dense(output_dim, activation="linear")

    @tf.function
    def call(self, inputs: tf.Tensor, targets: tf.Tensor):
        x = self.input_layer(inputs)
        x = self.model_body(x)
        x = self.linear_output(x)
        return x

    @tf.function
    def loss_func(self, outputs, targets) -> tf.Tensor:
        loss = tf.keras.losses.mean_squared_error(y_true = targets, y_pred = outputs)
        return loss

    @tf.function
    def compute_loss(self, inputs: tf.Tensor, targets: tf.Tensor) -> tf.Tensor:
        outputs = self.call(inputs, targets)
        return self.loss_func(outputs, targets)



model = Model(input_count, value_output_count + action_output_count, 32, 2)


optimizer = tf.keras.optimizers.Adam()
model.compile(optimizer = optimizer, loss = model.loss_func)


module_no_signatures_path = os.path.join('', 'module_no_signatures')
print('Saving model on path: ' + module_no_signatures_path)
print("LA L AL A")


tb_path = path_to_store + "/summary"

# tf.saved_model.save(model, path_to_store + "/summary5")

# train_writer = tf.summary.FileWriter(path_to_store + "/summary", sess.graph)
# train_writer.close()
# tf.saved_model.save(model, module_no_signatures_path)


# from tensorflow.python.ops import summary_ops_v2
# from tensorflow.python.keras.backend import get_graph

# tb_writer = tf.summary.create_file_writer(tb_path)
# with tb_writer.as_default():
#     summary_ops_v2.graph(get_graph())


model.save(tb_path)


with open(os.path.join(path_to_store, model_name + '.pb'), 'wb') as f:
    f.write(tf.compat.v1.get_default_graph().as_graph_def().SerializeToString())

